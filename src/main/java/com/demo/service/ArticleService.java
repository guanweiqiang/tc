package com.demo.service;

import com.demo.pojo.Article;

public interface ArticleService {

    boolean add(Article article);

    boolean update(Article article);

    boolean delete(Long id);

    Article searchDetail(Long id);

    Integer getCommentCount(Long id);

    Integer getLikeCount(Long id);

    String getAuthorName(Long id);
}
