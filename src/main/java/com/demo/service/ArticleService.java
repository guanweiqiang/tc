package com.demo.service;

import com.demo.pojo.Article;
import com.demo.pojo.dto.ArticleSearchListDTO;
import com.demo.pojo.vo.ArticleDetailVO;
import com.demo.pojo.vo.ArticleListVO;

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
