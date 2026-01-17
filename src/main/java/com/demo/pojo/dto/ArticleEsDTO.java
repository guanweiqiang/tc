package com.demo.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;

//todo:use this dto to store article in es
@Data
public class ArticleEsDTO {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Integer viewCount;
}
