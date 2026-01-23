package com.demo.model.dto;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class ArticleEsDTO {

    private Long id;
    private String title;
    private String content;


    private LocalDateTime createdAt;
    private Integer viewCount;
}
