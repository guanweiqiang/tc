package com.demo.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleDetailVO {


    private Long id;
    private String title;
    private String content;

    private Long authorId;
    private String authorName;

    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;

    private LocalDateTime createdAt;
}
