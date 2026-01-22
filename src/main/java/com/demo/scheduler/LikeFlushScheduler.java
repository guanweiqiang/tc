package com.demo.scheduler;

import com.demo.config.RedisLuaScriptManager;
import com.demo.pojo.RedisLuaScript;
import com.demo.service.ArticleLikeService;
import com.demo.service.Impl.ArticleLikeServiceImpl;
import io.lettuce.core.RedisCommandExecutionException;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


@Component
public class LikeFlushScheduler {

    @Resource
    private ArticleLikeService service;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisLuaScriptManager scriptManager;

    @Scheduled(fixedDelay = 5000)
    public void flushLike() {

        int current = redisTemplate.execute(
                scriptManager.get(RedisLuaScript.CURRENT_CHANGE, Long.class),
                List.of(ArticleLikeServiceImpl.CURRENT)
        ).intValue();

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(ArticleLikeServiceImpl.UPDATE))) {
            return;
        }
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();

        String frozenUpdate = ArticleLikeServiceImpl.UPDATE + ":v" + System.currentTimeMillis();
        try {
            redisTemplate.rename(ArticleLikeServiceImpl.UPDATE, frozenUpdate);
        } catch (RedisCommandExecutionException ignore) {
            return;
        }

        Cursor<Object> scan = setOps.scan(frozenUpdate, ScanOptions.scanOptions().count(100).build());
        while (scan.hasNext()) {
            Long id = Long.valueOf(scan.next().toString());
            Set<Object> newLikes = setOps.difference(
                    ArticleLikeServiceImpl.ADD + id + ":v" + current,
                    ArticleLikeServiceImpl.DEL + id + ":v" + current);

            Set<Object> newUnlikes = setOps.difference(
                    ArticleLikeServiceImpl.DEL + id + ":v" + current,
                    ArticleLikeServiceImpl.ADD + id + ":v" + current);

            boolean b = service.flushOneArticle(id, newLikes, newUnlikes);
            if (b) {
                setOps.getOperations().delete(ArticleLikeServiceImpl.ADD + id + ":v" + current);
                setOps.getOperations().delete(ArticleLikeServiceImpl.DEL + id + ":v" + current);
            }

        }
        scan.close();
        setOps.getOperations().delete(frozenUpdate);
    }
}
