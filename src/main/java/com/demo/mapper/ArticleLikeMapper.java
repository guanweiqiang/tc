package com.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleLikeMapper {

    List<Long> getLikedUsers(Long articleId);

    int batchInsert(@Param("articleId") Long articleId, @Param("userId") List<Long> userId);

    int batchDelete(@Param("articleId") Long articleId, @Param("userId") List<Long> userId);

    Integer getLikeCount(Long id);

}
