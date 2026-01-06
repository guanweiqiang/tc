package com.demo.mapper;

import com.demo.pojo.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper {

    int insert(Article article);

    int update(Article article);

    int delete(Long id);

    Article selectById(Long id);

    Integer getCommentCount(Long id);

    Integer getLikeCount(Long id);

    String getAuthorName(Long userId);
}
