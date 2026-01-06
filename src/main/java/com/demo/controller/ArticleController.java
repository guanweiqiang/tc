package com.demo.controller;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.demo.exception.GlobalException;
import com.demo.pojo.Article;
import com.demo.pojo.DTO.ArticleAddDTO;
import com.demo.pojo.DTO.ArticleSearchDetailDTO;
import com.demo.pojo.DTO.ArticleSearchListDTO;
import com.demo.pojo.DTO.ArticleUpdateDTO;
import com.demo.pojo.Response;
import com.demo.pojo.UserContext;
import com.demo.pojo.VO.ArticleDetailVO;
import com.demo.pojo.VO.ArticleListVO;
import com.demo.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("article")
public class ArticleController {

    @Resource
    private ArticleService service;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private Logger logger;

    @PostMapping("add")
    public Response<Void> add(@RequestBody ArticleAddDTO articleAddDTO) {
        Article article = new Article();
        article.setAuthorId(UserContext.get());
        BeanUtil.copyProperties(articleAddDTO, article);
        if (!service.add(article)) {
            throw new GlobalException("文章添加失败，请重试");
        }
        return Response.ok();
    }

    @PutMapping("update")
    public Response<Void> update(@RequestBody ArticleUpdateDTO articleUpdateDTO) {
        Article article = new Article();
        BeanUtil.copyProperties(articleUpdateDTO, article);
        if (!service.update(article)) {
            throw new GlobalException("文件修改失败，请重试");
        }
        return Response.ok();
    }

    //todo: use es to search
    @PostMapping("search")
    public Response<List<ArticleListVO>> search(@RequestBody ArticleSearchListDTO searchListDTO) {
        elasticsearchClient.search();
        return null;
    }

    @PostMapping("searchDetail")
    public Response<ArticleDetailVO> searchDetail(@RequestBody ArticleSearchDetailDTO searchDetailDTO) {
        Article article = service.searchDetail(searchDetailDTO.getId());
        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        BeanUtil.copyProperties(article, articleDetailVO);
        articleDetailVO.setLikeCount(service.getLikeCount(article.getId()));
        articleDetailVO.setCommentCount(service.getCommentCount(article.getId()));
        articleDetailVO.setAuthorName(service.getAuthorName(article.getAuthorId()));
        return Response.ok(articleDetailVO);
    }

    @DeleteMapping("delete/{id}")
    public Response<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            throw new GlobalException("文章删除失败");
        }
        return Response.ok();
    }


}
