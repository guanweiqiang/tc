package com.demo.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.demo.config.FileUploadProperties;
import com.demo.exception.comment.CommentFailException;
import com.demo.mapper.CommentMapper;
import com.demo.pojo.Comment;
import com.demo.pojo.dto.CommentAddDTO;
import com.demo.pojo.dto.CommentReplyDTO;
import com.demo.pojo.User;
import com.demo.pojo.UserContext;
import com.demo.pojo.dto.CommentUserDTO;
import com.demo.pojo.dto.SubCommentCountDTO;
import com.demo.pojo.vo.CommentVO;
import com.demo.service.CommentService;
import com.demo.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper mapper;

    @Resource
    private UserService userService;

    @Resource
    private FileUploadProperties properties;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public static final String COUNT = "article:comment:count:";


    @Override
    public void add(CommentAddDTO commentAddDTO) {
        Comment comment = new Comment();
        BeanUtil.copyProperties(commentAddDTO, comment);

        Long userId = UserContext.get();
        comment.setUserId(userId);

        int add = mapper.add(comment);
        if (add != 1) {
            log.error("文章id={}，userId={}添加评论失败",
                    comment.getArticleId(),
                    comment.getUserId());
            throw new CommentFailException("添加评论失败");
        }
        log.info("文章id={}，userId={}成功添加评论",
                comment.getArticleId(),
                comment.getUserId());
    }

    @Override
    public void reply(CommentReplyDTO commentReplyDTO) {
        Comment comment = new Comment();
        BeanUtil.copyProperties(commentReplyDTO, comment);

        Long userId = UserContext.get();
        comment.setUserId(userId);

        int reply = mapper.reply(comment);
        if (reply != 1) {
            log.error("一级评论id={}，userId={}回复评论id={}失败",
                    comment.getRootId(),
                    comment.getUserId(),
                    comment.getReplyToId());
            throw new CommentFailException("回复评论失败");
        }
        log.info("一级评论id={}，userId={}成功回复评论id={}",
                comment.getRootId(),
                comment.getUserId(),
                comment.getReplyToId());
    }

    @Override
    public List<CommentVO> getRootComment(Long articleId) {
        List<Comment> rootComment = mapper.getRootComment(articleId);
        Set<Long> userIds = rootComment.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());
//        log.info("userIds={}", userIds);
        Set<Long> rootIds = rootComment.stream()
                .map(Comment::getId)
                .collect(Collectors.toSet());
//        log.info("rootIds={}", rootIds);


        Map<Long, User> users = userService.getUserBatch(userIds);
//        log.info("users={}", users);

        Map<Long, SubCommentCountDTO> subCommentCount = getSubCommentCountBatch(rootIds);
//        log.info("subCommentCount={}", subCommentCount);

        List<CommentVO> commentVOs = rootComment.stream()
                .map(comment -> {
                    CommentVO commentVO = new CommentVO();
                    BeanUtil.copyProperties(comment, commentVO);

                    User user = users.get(comment.getUserId());

                    commentVO.setNickname(user.getNickname());
                    commentVO.setAvatar(userService.buildAvatar(user.getAvatarUrl()));

                    SubCommentCountDTO count = subCommentCount.getOrDefault(comment.getId(), null);
                    if (count != null) {
                        commentVO.setSubCount(count.getCount());
                    }
                    return commentVO;
                }).toList();


        log.info("获取articleId={}的评论成功", articleId);
        return commentVOs;
    }


    @Override
    public List<CommentVO> getSecondaryComment(Long rootId) {
        List<Comment> secondaryComment = mapper.getSecondaryComment(rootId);
        Set<Long> userIds = secondaryComment.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> users = userService.getUserBatch(userIds);
//        log.info("user={}", users);

        Set<Long> replyIds = secondaryComment.stream()
                .map(Comment::getReplyToId)
                .collect(Collectors.toSet());

        Map<Long, CommentUserDTO> userReply = getUserOfCommentBatch(replyIds);
//        log.info("reply={}", userReply);

        List<CommentVO> commentVOS = secondaryComment.stream()
                .map(comment -> {
                    CommentVO commentVO = new CommentVO();
                    BeanUtil.copyProperties(comment, commentVO);

                    User user = users.get(comment.getUserId());

                    commentVO.setNickname(user.getNickname());
                    commentVO.setAvatar(userService.buildAvatar(user.getAvatarUrl()));

                    CommentUserDTO reply = userReply.get(comment.getReplyToId());
                    commentVO.setReplyToName(reply.getNickname());
                    return commentVO;
                }).toList();


        log.info("获取rootId={}的次级评论成功", rootId);
        return commentVOS;
    }


    private List<CommentVO> fillCommentVO(List<Comment> comments) {

        return comments.stream()
                .map(comment -> {
                    CommentVO commentVO = new CommentVO();
                    BeanUtil.copyProperties(comment, commentVO);
                    Long userId = comment.getUserId();

                    commentVO.setNickname(userService.getNickname(userId));
                    commentVO.setAvatar(userService.getAvatar(userId));

                    if ((comment.getReplyToId()) != null) {
                        commentVO.setReplyToName(userService.getNickname(comment.getReplyToId()));
                        commentVO.setSubCount(getSucCommentCount(comment.getRootId()));
                    }

                    return commentVO;
                })
                .toList();
    }



    @Override
    public Integer getSucCommentCount(Long rootId) {
        return mapper.getSubCommentCount(rootId);
    }

    @Override
    public Map<Long, SubCommentCountDTO> getSubCommentCountBatch(Set<Long> rootId) {
        if (rootId == null || rootId.isEmpty()) {
            return Map.of();
        }

        List<SubCommentCountDTO> countBatch = mapper.getSubCommentCountBatch(rootId);
        return countBatch.stream()
                .collect(Collectors.toMap(
                        SubCommentCountDTO::getId,
                        dto -> dto
                ));
    }

    @Override
    public Long getUserOfComment(Long commentId) {

        return mapper.getUserOfComment(commentId);
    }

    @Override
    public Map<Long, CommentUserDTO> getUserOfCommentBatch(Set<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Map.of();
        }
        List<CommentUserDTO> users = mapper.getUserOfCommentBatch(commentIds);
        return users.stream()
                .collect(Collectors.toMap(
                        CommentUserDTO::getCommentId,
                        u -> u
                ));
    }

    private void ensureCountCache(Long articleId) {
        String key = COUNT + articleId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            return;
        }

        Integer count = getCommentCountFromDB(articleId);
        redisTemplate.opsForValue().set(key, count, 1, TimeUnit.MINUTES);

    }

    private Integer getCommentCountFromDB(Long articleId) {
        return mapper.getCommentCount(articleId);
    }


    @Override
    public Integer getCommentCount(Long articleId) {
        String countKey = COUNT + articleId;
        ensureCountCache(articleId);

        return (Integer) redisTemplate.opsForValue().get(countKey);
    }

    @Override
    public Map<Long, Integer> getCommentCountBatch(List<Long> articleIds) {
        articleIds.forEach(this::ensureCountCache);

        List<Object> list = redisTemplate.opsForValue().multiGet(
                articleIds.stream()
                        .map(id -> COUNT + id)
                        .toList()
        );

        if (list == null || list.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> map = new HashMap<>();
        for (int i = 0; i < articleIds.size(); i++) {
            if (list.get(i) == null) {
                continue;
            }
            map.put(articleIds.get(i), (Integer) list.get(i));
        }

        return map;
    }
}
