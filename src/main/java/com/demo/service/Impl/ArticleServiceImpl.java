package com.demo.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.demo.mapper.ArticleMapper;
import com.demo.pojo.Article;
import com.demo.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.logging.Logger;

@Service
public class ArticleServiceImpl implements ArticleService {

    public static final String ES_INDEX = "tc";

    @Resource
    private ArticleMapper mapper;

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private Logger logger;

    @Override
    public boolean add(Article article) {
        int insert = mapper.insert(article);
        if (insert != 1) {
            logger.severe("文章添加到mysql中失败");
            return false;
        }
        try {
            elasticsearchClient.index(i -> i
                    .index(ES_INDEX)
                    .id(article.getId().toString())
                    .document(article)
            );
        } catch (IOException e) {
            logger.severe("文章加入elasticsearch失败！");
        }
        logger.info("文章" + article.getId() + "加入es成功");
        return true;
    }

    @Override
    public boolean update(Article article) {
        return mapper.update(article) == 1;
    }

    @Override
    public Article searchDetail(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public Integer getCommentCount(Long id) {
        return mapper.getCommentCount(id);
    }

    @Override
    public Integer getLikeCount(Long id) {
        return mapper.getLikeCount(id);
    }

    @Override
    public String getAuthorName(Long userId) {
        return mapper.getAuthorName(userId);
    }

    @Override
    public boolean delete(Long id) {
        return mapper.delete(id) == 1;
    }
}
