package com.demo.controller;

import com.demo.pojo.Response;
import com.demo.pojo.UserContext;
import com.demo.service.ArticleLikeService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("article/like")
public class ArticleLikeController {

    @Resource
    private ArticleLikeService service;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public static final String ADD = "article:like:add:";
    public static final String DEL = "article:like:del:";
    public static final String UPDATE = "article:like:update";
    public static final String CURRENT = "article:like:current";

    @PostConstruct
    public void post() {
        redisTemplate.opsForValue().set(CURRENT, 1);
    }

    @GetMapping("{id}")
    public Response<Integer> getLikeCount(@PathVariable("id") Long id) {
        return Response.ok(service.getLikeCount(id));
    }

    @PostMapping("{id}")
    public Response<Void> recordLike(@PathVariable("id") Long id) {
        Long userId = UserContext.get();
        Integer current = (Integer) redisTemplate.opsForValue().get(CURRENT);
        redisTemplate.opsForSet().add(ADD + id + ":v" + current, userId);
        redisTemplate.opsForSet().add(UPDATE, id);
        return Response.ok();
    }

    @PostMapping("/not/{id}")
    public Response<Void> recordUnlike(@PathVariable("id") Long id) {
        Long userId = UserContext.get();
        Integer current = (Integer)redisTemplate.opsForValue().get(CURRENT);
        redisTemplate.opsForSet().add(DEL + id + ":v" + current, userId);
        redisTemplate.opsForSet().add(UPDATE, id);
        return Response.ok();
    }


    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void flushLike() {
        Integer current = (Integer) redisTemplate.opsForValue().get(CURRENT);
        redisTemplate.opsForValue().set(CURRENT, current ^ 1);

        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> members = setOps.members(UPDATE);
        setOps.getOperations().delete(UPDATE);
        if (members == null || members.isEmpty()) {
            return;
        }

        for (Object member : members) {
            Long id = Long.valueOf(member.toString());

            Set<Object> newLikes = setOps.difference(ADD + id + ":v" + current, DEL + id + ":v" + current);

            Set<Object> newUnlikes = setOps.difference(DEL + id + ":v" + current, ADD + id + ":v" + current);

            service.batchInsert(id, newLikes);
            service.batchDelete(id, newUnlikes);

            setOps.getOperations().delete(ADD + id + ":v" + current);
            setOps.getOperations().delete(DEL + id + ":v" + current);
        }
    }



}
