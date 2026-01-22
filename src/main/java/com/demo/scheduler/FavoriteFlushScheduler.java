package com.demo.scheduler;

import com.demo.config.RedisLuaScriptManager;
import com.demo.pojo.RedisLuaScript;
import com.demo.service.ArticleFavoriteService;
import com.demo.service.Impl.ArticleFavoriteServiceImpl;
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
public class FavoriteFlushScheduler {

    @Resource
    private ArticleFavoriteService service;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisLuaScriptManager scriptManager;


    //todo: use rabbitmq to flush db
    @Scheduled(fixedDelay = 5000)
    public void flushLike() {

        //change the current version
        int current = redisTemplate.execute(
                scriptManager.get(RedisLuaScript.CURRENT_CHANGE, Long.class),
                List.of(ArticleFavoriteServiceImpl.CURRENT)
        ).intValue();

        //judge if there is new records
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(ArticleFavoriteServiceImpl.UPDATE))) {
            return;
        }

        //frozen the update
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        String frozenUpdate = ArticleFavoriteServiceImpl.UPDATE + ":v" + System.currentTimeMillis();
        try {
            redisTemplate.rename(ArticleFavoriteServiceImpl.UPDATE, frozenUpdate);
        } catch (RedisCommandExecutionException ignore) {
            return;
        }

        Cursor<Object> scan = setOps.scan(frozenUpdate, ScanOptions.scanOptions().count(100).build());
        while (scan.hasNext()) {
            Long id = Long.valueOf(scan.next().toString());

            Set<Object> newFavorites = setOps.difference(
                    ArticleFavoriteServiceImpl.ADD + id + ":v" + current,
                    ArticleFavoriteServiceImpl.DEL + id + ":v" + current);

            Set<Object> newUnFavorites = setOps.difference(
                    ArticleFavoriteServiceImpl.DEL + id + ":v" + current,
                    ArticleFavoriteServiceImpl.ADD + id + ":v" + current);

            boolean success = service.flushOneArticle(id, newFavorites, newUnFavorites);
            if (success) {
                setOps.getOperations().delete(ArticleFavoriteServiceImpl.ADD + id + ":v" + current);
                setOps.getOperations().delete(ArticleFavoriteServiceImpl.DEL + id + ":v" + current);
            }

        }
        scan.close();

        setOps.getOperations().delete(frozenUpdate);

    }
}
