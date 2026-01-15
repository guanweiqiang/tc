package com.demo.service;

import com.demo.pojo.Article;
import com.demo.pojo.DTO.ArticleSearchListDTO;
import com.demo.pojo.VO.ArticleDetailVO;
import com.demo.pojo.VO.ArticleListVO;

import java.util.List;

public interface ArticleService {

    boolean add(Article article);

    boolean update(Article article);

    boolean delete(Long id);

    List<ArticleListVO> searchList(ArticleSearchListDTO dto);

    ArticleDetailVO searchDetail(Long id);

    Integer getCommentCount(Long id);

    String getAuthorName(Long id);
}
