package com.demo.mapper;

import com.demo.pojo.Comment;
import com.demo.model.dto.CommentUserDTO;
import com.demo.model.dto.SubCommentCountDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface CommentMapper {

    int add(Comment comment);

    int reply(Comment comment);

    List<Comment> getRootComment(Long articleId);

    List<Comment> getSecondaryComment(Long rootId);

    Integer getSubCommentCount(Long rootId);


    List<SubCommentCountDTO> getSubCommentCountBatch(
            @Param("rootIds") Set<Long> rootIds);

    Long getUserOfComment(Long commentId);


    List<CommentUserDTO> getUserOfCommentBatch(
            @Param("commentIds") Set<Long> commentIds);

    Integer getCommentCount(Long articleId);


}
