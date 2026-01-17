package com.demo.service;

import com.demo.pojo.dto.CommentAddDTO;
import com.demo.pojo.dto.CommentReplyDTO;
import com.demo.pojo.User;
import com.demo.pojo.dto.CommentUserDTO;
import com.demo.pojo.dto.SubCommentCountDTO;
import com.demo.pojo.vo.CommentVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommentService {

    /**
     * Add comment.
     * @param commentAddDTO The dto of add comment.
     */
    void add(CommentAddDTO commentAddDTO);

    /**
     * Reply comment.
     * @param commentReplyDTO The dto of reply.
     */
    void reply(CommentReplyDTO commentReplyDTO);

    /**
     * Get the root comment by id.
     * @param articleId The id of article.
     * @return List of comment view.
     */
    List<CommentVO> getRootComment(Long articleId);

    /**
     * Get the secondary comment by root id.
     * @param rootId The id of root comment.
     * @return List of comment.
     */
    List<CommentVO> getSecondaryComment(Long rootId);

    /**
     * Get the sub comment count of the root article.
     * @param rootId The id of root article.
     * @return The count of sub comment.
     */
    Integer getSucCommentCount(Long rootId);

    /**
     * Get the count of sub comment batch.
     * @return The map.
     */
    Map<Long, SubCommentCountDTO> getSubCommentCountBatch(
            Set<Long> rootId);

    /**
     * Get the id of user by the comment id.
     * @param commentId The id of comment.
     * @return The id of user who send the comment.
     */
    Long getUserOfComment(Long commentId);


    /**
     * Get the user info of comment batch.
     * @param commentIds The set of comment id.
     * @return A map, key is the comment id, value is userinfo.
     */
    Map<Long, CommentUserDTO> getUserOfCommentBatch(
            Set<Long> commentIds);


    /**
     * Get the comment count.
     * @param articleId The id of comment.
     * @return The count of comment.
     */
    Integer getCommentCount(Long articleId);

    /**
     * Get the comment count batch.
     * @param articleIds The set of article id.
     * @return A map, key is the article id, value is count of comment.
     */
    Map<Long, Integer> getCommentCountBatch(List<Long> articleIds);

}
