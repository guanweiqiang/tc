package com.demo.service.Impl;

import com.demo.mapper.ArticleLikeMapper;
import com.demo.service.ArticleLikeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


@Service
public class ArticleLikeServiceImpl implements ArticleLikeService {

    @Resource
    private ArticleLikeMapper mapper;


    @Override
    public List<Long> getLikedUsers(Long articleId) {
        return mapper.getLikedUsers(articleId);
    }

    @Override
    public boolean batchInsert(Long id, Set<Object> userIds) {

        if (userIds.isEmpty()) {
            return true;
        }
        List<Long> list = userIds.parallelStream().map(x -> Long.valueOf(x.toString())).toList();
        return mapper.batchInsert(id, list) == list.size();
    }

    @Override
    public boolean batchDelete(Long id, Set<Object> userIds) {
        if (userIds.isEmpty()) {
            return true;
        }
        List<Long> list = userIds.parallelStream().map(x -> Long.valueOf(x.toString())).toList();
        return mapper.batchDelete(id, list) == list.size();
    }


    @Override
    public Integer getLikeCount(Long id) {

        return mapper.getLikeCount(id);
    }
}
