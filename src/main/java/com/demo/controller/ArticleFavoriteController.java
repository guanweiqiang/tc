package com.demo.controller;

import com.demo.advice.BizLog;
import com.demo.pojo.Response;
import com.demo.pojo.UserContext;
import com.demo.service.ArticleFavoriteService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("article/favorite")
public class ArticleFavoriteController {


    @Resource
    private ArticleFavoriteService service;

    @PostMapping("{id}")
    @BizLog("favorite")
    public Response<Boolean> favorite(@PathVariable("id") Long articleId) {
        Boolean favorite = service.favorite(articleId);
        return Response.ok(favorite);
    }

    @GetMapping("{id}")
    @BizLog("isFavorite")
    public Response<Boolean> isFavorite(@PathVariable("id") Long articleId) {
        Long userId = UserContext.get();
        return Response.ok(service.isFavorite(articleId, userId));
    }
}
