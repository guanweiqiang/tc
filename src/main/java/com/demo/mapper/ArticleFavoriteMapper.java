package com.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface ArticleFavoriteMapper {

    List<Long> getFavoriteUsers(Long articleId);

    int batchInsert(@Param("articleId") Long articleId, @Param("userIds") Set<Object> userIds);

    int batchDelete(@Param("articleId") Long articleId, @Param("userIds") Set<Object> userIds);
}
