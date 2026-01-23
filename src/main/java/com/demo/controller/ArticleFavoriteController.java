package com.demo.controller;

import com.demo.advice.BizLog;
import com.demo.pojo.Response;
import com.demo.pojo.UserContext;
import com.demo.service.ArticleFavoriteService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("article/favorite")
@Validated
public class ArticleFavoriteController {


    @Resource
    private ArticleFavoriteService service;

    @PostMapping("{id}")
    @BizLog("favorite")
    public Response<Boolean> favorite(
            @PathVariable("id")
            @NotNull
            @Positive(message = "文章id必须为正数")Long articleId) {
        Boolean favorite = service.favorite(articleId);
        return Response.ok(favorite);
    }

    @GetMapping("{id}")
    @BizLog("isFavorite")
    public Response<Boolean> isFavorite(
            @PathVariable("id")
            @NotNull
            @Positive(message = "文章id必须为正数")Long articleId) {
        Long userId = UserContext.get();
        return Response.ok(service.isFavorite(articleId, userId));
    }
}
