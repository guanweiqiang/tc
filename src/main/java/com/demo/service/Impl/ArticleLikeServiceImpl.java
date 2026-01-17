package com.demo.service.Impl;

import com.demo.config.RedisLuaScriptManager;
import com.demo.mapper.ArticleLikeMapper;
import com.demo.pojo.RedisLuaScript;
import com.demo.pojo.UserContext;
import com.demo.service.ArticleLikeService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ArticleLikeServiceImpl implements ArticleLikeService {

    @Resource
    private ArticleLikeMapper mapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
    public static final String COUNT = "article:like:count:";

    //init is used to record whether the LIKE state of article is in redis
    //because redis will auto delete the set key if empty,
    //you can't judge by the key itself
    public static final String INIT = "article:like:init:";


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
        String initKey = INIT + articleId;
        String countKey = COUNT + articleId;

        int ttl = 60;


        Long res = stringRedisTemplate.execute(
                scriptManager.get(RedisLuaScript.ENSURE_CACHE, Long.class),
                List.of(likeKey, countKey, initKey),
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

        List<Long> userIds = getLikedUsers(articleId);
        log.debug("articleId={}, userIds={}", articleId, userIds);
        if (userIds.isEmpty()) {
            return;
        }

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(ttl));
        for (Long userId : userIds) {
            args.add(String.valueOf(userId));
        }

        //must use stringRedisTemplate or diy the redisTemplate
        //because lua will treat the array as an object
        stringRedisTemplate.execute(
                scriptManager.get(RedisLuaScript.COMMIT_CACHE, Void.class),
                List.of(likeKey, countKey, initKey),
                args.toArray()
        );
        log.debug("将id={}的文章点赞加入缓存", articleId);


//        if (Boolean.TRUE.equals(redisTemplate.hasKey(initKey))) {
//            log.info("已存在key{}，更新过期时间", likeKey);
//            redisTemplate.expire(likeKey, 1, TimeUnit.MINUTES);
//            redisTemplate.expire(initKey, 1, TimeUnit.MINUTES);
//            redisTemplate.expire(countKey, 1, TimeUnit.MINUTES);
//            return;
//        }
//
//
//        List<Long> userIds = getLikedUsers(articleId);
//        if (!userIds.isEmpty()) {
//            log.info("冷加载，{}，将db中的数据加入redis中", likeKey);
//            redisTemplate.opsForSet().add(likeKey, userIds.toArray());
//            redisTemplate.opsForValue().set(initKey, 1);
//            redisTemplate.opsForValue().set(countKey, userIds.size(), 1, TimeUnit.MINUTES);
//
//
//            redisTemplate.expire(likeKey, 1, TimeUnit.MINUTES);
//            redisTemplate.expire(initKey, 1, TimeUnit.MINUTES);
//
//        }

    }

    @Override
    public Boolean recordLike(Long id) {
        Long userId = UserContext.get();

        Integer current = (Integer) redisTemplate.opsForValue().get(CURRENT);
        String likeKey = LIKE + id;
        String addKey = ADD + id + ":v" + current;
        String delKey = DEL + id + ":v" + current;

        ensureLikeCache(id);
        //first, check redis
        //if exists, remove

        //use lua to implement atomic
        Long result = redisTemplate.execute(
                scriptManager.get(RedisLuaScript.ARTICLE_LIKE, Long.class),
                List.of(likeKey, addKey, delKey, UPDATE),
                userId, id
        );


        return result != null && result == 1;
    }

    @SuppressWarnings("unused")
    private void unlike(Long id, Long userId) {
        Integer current = (Integer)redisTemplate.opsForValue().get(CURRENT);
        redisTemplate.opsForSet().add(DEL + id + ":v" + current, userId);
        redisTemplate.opsForSet().add(UPDATE, id);
        redisTemplate.opsForSet().remove(ADD + id + ":v" + current, userId);
    }

    @Override
    public void batchInsert(Long id, Set<Object> userIds) {

        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<Long> list = userIds.parallelStream().map(x -> Long.valueOf(x.toString())).toList();
        try {
             mapper.batchInsert(id, list);
        } catch (DuplicateKeyException ignore) {
            log.warn("重复插入，可能有不允许的行为发生");
        }

    }

    @Override
    public void batchDelete(Long id, Set<Object> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<Long> list = userIds.parallelStream().map(x -> Long.valueOf(x.toString())).toList();
        mapper.batchDelete(id, list);
    }




    @Override
    public Integer getLikeCount(Long id) {
        String countKey = COUNT + id;
        ensureLikeCache(id);

        return (Integer) redisTemplate.opsForValue().get(countKey);
    }

    @Override
    public Map<Long, Integer> getLikeCountBatch(List<Long> articleIds) {
        articleIds.forEach(this::ensureLikeCache);

        List<Object> list = redisTemplate.opsForValue().multiGet(
                articleIds.stream()
                        .map(id -> COUNT + id)
                        .collect(Collectors.toList())
        );
        log.debug("list={}", list);

        if (list == null || list.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> map = new HashMap<>();
        for (int i = 0; i < articleIds.size(); i++) {
            if (list.get(i) == null) {

                continue;
            }
            map.put(articleIds.get(i), Integer.valueOf(list.get(i).toString()));
        }
        log.debug("map={}", map);
        return map;
    }

    @Transactional
    public Boolean flushOneArticle(Long articleId,
                                   Set<Object> newLikes,
                                   Set<Object> newUnlikes) {

        try {
            batchInsert(articleId, newLikes);
            batchDelete(articleId, newUnlikes);
            return true;
        } catch (Exception e) {
            log.error("定时刷盘点赞有误");
        }

        return false;
    }





}
