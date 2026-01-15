package com.demo.service.Impl;

import com.demo.config.RedisLuaScriptManager;
import com.demo.mapper.ArticleLikeMapper;
import com.demo.pojo.RedisLuaScript;
import com.demo.pojo.UserContext;
import com.demo.service.ArticleLikeService;
import io.lettuce.core.RedisCommandExecutionException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class ArticleLikeServiceImpl implements ArticleLikeService {

    @Resource
    private ArticleLikeMapper mapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisLuaScriptManager scriptManager;

    //use like to stand for the real time state
    //add to record new likes
    //del to record new unlikes
    //update to record the ids of articles changed
    //current is the current set used
    public static final String ADD = "article:like:add:";
    public static final String DEL = "article:like:del:";
    public static final String UPDATE = "article:like:update:";
    public static final String CURRENT = "article:like:current";
    public static final String LIKE = "article:like:";

    @PostConstruct
    public void post() {
        redisTemplate.opsForValue().set(CURRENT, 1);
    }


    @Override
    public List<Long> getLikedUsers(Long articleId) {
        return mapper.getLikedUsers(articleId);
    }

    @Override
    public Boolean isLiked(Long id, Long userId) {

        ensureLikeCache(id);

        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(LIKE + id, userId));
    }

    private void ensureLikeCache(Long articleId) {
        String likeKey = LIKE + articleId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(likeKey))) {
            return;
        }

        List<Long> userIds = getLikedUsers(articleId);
        if (!userIds.isEmpty()) {
            redisTemplate.opsForSet().add(likeKey, userIds.toArray());
            redisTemplate.expire(likeKey, 1, TimeUnit.MINUTES);
        }

    }

    @Override
    public Boolean recordLike(Long id) {
        Long userId = UserContext.get();

        ensureLikeCache(id);


        Integer current = (Integer) redisTemplate.opsForValue().get(CURRENT);
        String likeKey = LIKE + id;
        String addKey = ADD + id + "v" + current;
        String delKey = ADD + id + "v" + current;
        //first, check redis
        //if exists, remove

        //use lua to implement atomic
        return redisTemplate.execute(
                scriptManager.get(RedisLuaScript.ARTICLE_LIKE, Boolean.class),
                List.of(likeKey, addKey, delKey, UPDATE),
                userId, id
        );
    }

    private void unlike(Long id, Long userId) {
        Integer current = (Integer)redisTemplate.opsForValue().get(CURRENT);
        redisTemplate.opsForSet().add(DEL + id + ":v" + current, userId);
        redisTemplate.opsForSet().add(UPDATE, id);
        redisTemplate.opsForSet().remove(ADD + id + ":v" + current, userId);
    }

    @Override
    public boolean batchInsert(Long id, Set<Object> userIds) {

        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        List<Long> list = userIds.parallelStream().map(x -> Long.valueOf(x.toString())).toList();
        try {
             mapper.batchInsert(id, list);
             return true;
        } catch (DuplicateKeyException ignore) {
            log.warn("重复插入，可能有不允许的行为发生");
        }
        return false;

    }

    @Override
    public boolean batchDelete(Long id, Set<Object> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return true;
        }
        List<Long> list = userIds.parallelStream().map(x -> Long.valueOf(x.toString())).toList();
        return mapper.batchDelete(id, list) == list.size();
    }




    @Override
    public Integer getLikeCount(Long id) {

        ensureLikeCache(id);

        return redisTemplate.opsForSet().size(LIKE + id).intValue();
    }


    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void flushLike() {

        int current = redisTemplate.execute(
                scriptManager.get(RedisLuaScript.CURRENT_CHANGE, Long.class),
                List.of(CURRENT)
        ).intValue();

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(UPDATE))) {
            return;
        }

        SetOperations<String, Object> setOps = redisTemplate.opsForSet();

        String frozenUpdate = UPDATE + "v" + "old";
        try {
            redisTemplate.rename(UPDATE, frozenUpdate);
        } catch (RedisCommandExecutionException ignore) {
            return;
        }

        Set<Object> members = setOps.members(frozenUpdate);

        if (members == null || members.isEmpty()) {
            return;
        }

        for (Object member : members) {
            Long id = Long.valueOf(member.toString());

            Set<Object> newLikes = setOps.difference(ADD + id + ":v" + current, DEL + id + ":v" + current);

            Set<Object> newUnlikes = setOps.difference(DEL + id + ":v" + current, ADD + id + ":v" + current);

            batchInsert(id, newLikes);
            batchDelete(id, newUnlikes);

            setOps.getOperations().delete(ADD + id + ":v" + current);
            setOps.getOperations().delete(DEL + id + ":v" + current);
        }
        setOps.getOperations().delete(frozenUpdate);

    }


}
