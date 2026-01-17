package com.demo.service.Impl;

import com.demo.config.RedisLuaScriptManager;
import com.demo.mapper.ArticleFavoriteMapper;
import com.demo.pojo.RedisLuaScript;
import com.demo.pojo.UserContext;
import com.demo.service.ArticleFavoriteService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class ArticleFavoriteServiceImpl implements ArticleFavoriteService {

    @Resource
    private ArticleFavoriteMapper mapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisLuaScriptManager scriptManager;

    public static final String FAVORITE = "article:favorite:";
    public static final String ADD = "article:favorite:add:";
    public static final String DEL = "article:favorite:del:";
    public static final String UPDATE = "article:favorite:update";
    public static final String CURRENT = "article:favorite:current";
    public static final String COUNT = "article:favorite:count:";
    public static final String INIT = "article:favorite:init:";

    @PostConstruct
    public void post() {
        redisTemplate.opsForValue().set(CURRENT, 1);
    }


    @Override
    public List<Long> getFavoriteUsers(Long articleId) {
        return mapper.getFavoriteUsers(articleId);
    }

    @Override
    public Boolean isFavorite(Long articleId, Long userId) {
        String favoriteKey = FAVORITE + articleId;
        ensureFavoriteCache(articleId);

        return redisTemplate.opsForSet().isMember(favoriteKey, userId);
    }

    private void ensureFavoriteCache(Long articleId) {
        String key = FAVORITE + articleId;
        String countKey = COUNT + articleId;
        String initKey = INIT + articleId;

        int ttl = 60;

        Long res = stringRedisTemplate.execute(
                scriptManager.get(RedisLuaScript.ENSURE_CACHE, Long.class),
                List.of(key, countKey, initKey),
                String.valueOf(ttl), "3"
        );

        Objects.requireNonNull(res);
        //ready
        if (res == 1) {
            log.debug("文章点赞id={}已在缓存，更新过期时间", articleId);
            return;
        } else if (res == 2) {
            //loading
            return;
        }

        List<Long> userIds = getFavoriteUsers(articleId);
        log.debug("articleId={}, userIds={}", articleId, userIds);
        if (userIds.isEmpty()) {
            return;
        }

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(ttl));
        for (Long userId : userIds) {
            args.add(String.valueOf(userId));
        }


        stringRedisTemplate.execute(
                scriptManager.get(RedisLuaScript.COMMIT_CACHE, Void.class),
                List.of(key, countKey, initKey),
                args.toArray()
        );
        log.debug("将id={}的文章点赞加入缓存", articleId);


//        if (Boolean.TRUE.equals(redisTemplate.hasKey(initKey))) {
//            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
//            redisTemplate.expire(initKey, 1, TimeUnit.MINUTES);
//            redisTemplate.expire(countKey, 1, TimeUnit.MINUTES);
//            return;
//        }
//
//        List<Long> favoriteUsers = getFavoriteUsers(articleId);
//        if (!favoriteUsers.isEmpty()) {
//            redisTemplate.opsForValue().set(countKey, favoriteUsers.size());
//            redisTemplate.opsForValue().set(initKey, 1);
//            redisTemplate.opsForSet().add(key, favoriteUsers.toArray());
//
//            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
//            redisTemplate.expire(initKey, 1, TimeUnit.MINUTES);
//            redisTemplate.expire(countKey, 1, TimeUnit.MINUTES);
//        }


    }



    @Override
    public Boolean favorite(Long articleId) {
        Long userId = UserContext.get();

        Integer current = (Integer) redisTemplate.opsForValue().get(CURRENT);
        String favoriteKey = FAVORITE + articleId;
        String addKey = ADD + articleId + ":v" + current;
        String delKey = DEL + articleId + ":v" + current;
        String countKey = COUNT + articleId;


        ensureFavoriteCache(articleId);

        //use lua to implements atomic operation
        Long result = redisTemplate.execute(
                scriptManager.get(RedisLuaScript.ARTICLE_FAVORITE, Long.class),
                List.of(favoriteKey, addKey, delKey, countKey, UPDATE),
                userId, articleId
        );
        return result != null && result == 1;
    }

    @Override
    public Integer getFavoriteCount(Long articleId) {
        ensureFavoriteCache(articleId);
        Object value = redisTemplate.opsForValue().get(COUNT + articleId);
        if (value == null) {
            return 0;
        }
        return (Integer) value;
    }

    @Override
    public Map<Long, Integer> getFavoriteCountBatch(List<Long> articleIds) {
        articleIds.forEach(this::ensureFavoriteCache);

        List<Object> list = redisTemplate.opsForValue().multiGet(
                articleIds.stream()
                        .map(id -> COUNT + id)
                        .toList()
        );

        if (list == null || list.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> map = new HashMap<>();
        for (int i = 0; i < articleIds.size(); i++) {
            if (list.get(i) == null) {
                continue;
            }
            map.put(articleIds.get(i), (Integer) list.get(i));
        }


        return map;
    }

    @Transactional
    public Boolean flushOneArticle(Long articleId, Set<Object> likes, Set<Object> unLikes) {
        try {
            batchInsert(articleId, likes);
            batchDelete(articleId, unLikes);
            return true;
        } catch (Exception e) {
            log.warn("定时刷新收藏有误");
        }


        return false;
    }

    private void batchInsert(Long articleId, Set<Object> likes) {
        if (likes != null && !likes.isEmpty()) {
            mapper.batchInsert(articleId, likes);
            log.info("succeed to batch insert favorites.");
        }
    }

    private void batchDelete(Long articleId, Set<Object> unlikes) {
        if (unlikes != null && !unlikes.isEmpty()) {
            mapper.batchDelete(articleId, unlikes);
            log.info("succeed to batch delete favorites.");
        }
    }
}
