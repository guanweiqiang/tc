package com.demo.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ArticleFavoriteService {

    /**
     * Get all user who favorites the article.
     * @param article The id of the article.
     * @return The list of id of users.
     */
    List<Long> getFavoriteUsers(Long article);

    /**
     * Change the collect state of the article.
     * @param articleId The id of article.
     * @return True if collected, otherwise false.
     */
    Boolean favorite(Long articleId);

    /**
     * Get the count of favorite of article.
     * @param articleId The id of article.
     * @return The count of favorite.
     */
    Integer getFavoriteCount(Long articleId);


    /**
     * Get the count of favorite batch.
     * @param articleIds The set of article id.
     * @return A map, key is the article id, value is the count.
     */
    Map<Long, Integer> getFavoriteCountBatch(List<Long> articleIds);

    /**
     * Get whether the article is favorite of the user
     * @param articleId The id of article.
     * @param userId The id of user.
     * @return True if favorite, otherwise false.
     */
    Boolean isFavorite(Long articleId, Long userId);


    /**
     * Flush one article likes.
     * @param articleId The id of article.
     * @param favorites The set of new favorites.
     * @param unFavorites The set of new unFavorites
     * @return True is success, otherwise false.
     */
    Boolean flushOneArticle(Long articleId,
                            Set<Object> favorites,
                            Set<Object> unFavorites);

}
