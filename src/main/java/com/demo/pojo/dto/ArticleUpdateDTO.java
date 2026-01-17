package com.demo.pojo.dto;

import lombok.Data;

@Data
public class ArticleUpdateDTO {

    private Integer id;
    private String title;
    private String content;
}
