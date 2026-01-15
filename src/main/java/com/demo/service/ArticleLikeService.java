package com.demo.service;

import java.util.List;
import java.util.Set;

public interface ArticleLikeService {

    /**
     * Get the id of users which like the article.
     * @param articleId The article id;
     * @return A list of userId;
     */
    List<Long> getLikedUsers(Long articleId);

    /**
     * Get the count of likes of article.
     * @param articleId The id of article.
     * @return The count of likes.
     */
    Integer getLikeCount(Long articleId);

    /**
     * Batch insert into the table;
     * @param id The article id;
     * @param userIds The id of users.
     * @return True if success, otherwise false.
     */
    boolean batchInsert(Long id, Set<Object> userIds);


    /**
     * Batch delete from the table;
     * @param id The article id;
     * @param userIds The id of uses;
     * @return True if success, other false;
     */
    boolean batchDelete(Long id, Set<Object> userIds);

    /**
     * Get if the article liked by the user.
     * @param id The id of article.
     * @param userId The id of user;
     * @return True if liked, otherwise false.
     */
    Boolean isLiked(Long id, Long userId);

    /**
     * Liked or unlike.
     * @param id The id of article.
     * @return True if the action is like, otherwise false.
     */
    Boolean recordLike(Long id);
}
