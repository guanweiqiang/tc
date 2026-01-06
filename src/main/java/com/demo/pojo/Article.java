package com.demo.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Article {

    private Long id;
    private Long authorId;
    private String title;
    private String content;
    private Byte status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
