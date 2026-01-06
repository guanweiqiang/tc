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
     * Get the count of the article.
     * @param id The id of the article.
     * @return The count of like(s).
     */
    Integer getLikeCount(Long id);
}
