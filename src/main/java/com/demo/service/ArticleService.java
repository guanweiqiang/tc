package com.demo.service;

import com.demo.pojo.Article;
import com.demo.model.dto.ArticleSearchListDTO;
import com.demo.model.vo.ArticleDetailVO;
import com.demo.model.vo.ArticleListVO;

import java.util.List;

public interface ArticleService {

    boolean add(Article article);

    boolean update(Article article);

    boolean delete(Long id);

    List<ArticleListVO> searchList(ArticleSearchListDTO dto);

    ArticleDetailVO searchDetail(Long id);

    Integer getCommentCount(Long id);

//    Map<Long, Integer> getCommentCountBatch();

    String getAuthorName(Long id);
}
