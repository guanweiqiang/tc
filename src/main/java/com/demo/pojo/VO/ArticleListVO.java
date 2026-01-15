package com.demo.pojo.VO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleListVO {

    private Long id;
    private String title;

    private Long authorId;
    private String authorName;

    private Integer likeCount;
    private Boolean isLiked;
    private Integer favoriteCount;
    private Boolean isFavorite;
    private Integer commentCount;

    private LocalDateTime createdAt;

}
