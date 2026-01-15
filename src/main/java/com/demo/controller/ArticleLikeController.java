package com.demo.controller;

import com.demo.advice.BizLog;
import com.demo.pojo.Response;
import com.demo.service.ArticleLikeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("article/like")
@Slf4j
public class ArticleLikeController {

    @Resource
    private ArticleLikeService service;


    @GetMapping("{id}")
    @BizLog("get the count of like")
    public Response<Integer> getLikeCount(@PathVariable("id") Long id) {
        return Response.ok(service.getLikeCount(id));
    }

    @PostMapping("{id}")
    @BizLog("like or unlike the article")
    public Response<Boolean> recordLike(@PathVariable("id") Long id) {
        Boolean b = service.recordLike(id);
        if (b) {
            log.info("为文章id:{}点赞成功", id);
        } else {
            log.info("文章id:{}取消点赞成功", id);
        }
        return Response.ok(b);
    }







}
