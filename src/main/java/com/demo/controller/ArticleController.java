package com.demo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.demo.advice.BizLog;
import com.demo.common.exception.GlobalException;
import com.demo.pojo.Article;
import com.demo.model.dto.ArticleAddDTO;
import com.demo.model.dto.ArticleSearchListDTO;
import com.demo.model.dto.ArticleUpdateDTO;
import com.demo.pojo.Response;
import com.demo.pojo.UserContext;
import com.demo.model.vo.ArticleDetailVO;
import com.demo.model.vo.ArticleListVO;
import com.demo.service.ArticleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("article")
@Validated
public class ArticleController {

    @Resource
    private ArticleService service;



    @PostMapping("add")
    @BizLog("add article")
    public Response<Void> add(@Valid @RequestBody ArticleAddDTO articleAddDTO) {
        Article article = new Article();
        article.setAuthorId(UserContext.get());
        BeanUtil.copyProperties(articleAddDTO, article);
        if (!service.add(article)) {
            throw new GlobalException("文章添加失败，请重试");
        }
        return Response.ok();
    }

    @PutMapping("update")
    @BizLog("update article")
    public Response<Void> update(@Valid @RequestBody ArticleUpdateDTO articleUpdateDTO) {
        Article article = new Article();
        BeanUtil.copyProperties(articleUpdateDTO, article);
        if (!service.update(article)) {
            throw new GlobalException("文件修改失败，请重试");
        }
        return Response.ok();
    }


    @PostMapping("search")
    @BizLog("search articles")
    public Response<List<ArticleListVO>> search(@RequestBody ArticleSearchListDTO searchListDTO) {

        List<ArticleListVO> articleListVOS = service.searchList(searchListDTO);
        log.info("成功查询，返回");
        return Response.ok(articleListVOS);
    }

    @GetMapping("detail/{id}")
    @BizLog("getRootComment the detail of article")
    public Response<ArticleDetailVO> searchDetail(
            @PathVariable
            @NotNull
            @Positive(message = "文章id必须为正数")Long id) {
        return Response.ok(service.searchDetail(id));
    }

    @DeleteMapping("delete/{id}")
    @BizLog("delete the article")
    public Response<Void> delete(
            @PathVariable
            @NotNull
            @Positive(message = "文章id必须为正数")Long id) {
        if (!service.delete(id)) {
            throw new GlobalException("文章删除失败");
        }
        return Response.ok();
    }


}
