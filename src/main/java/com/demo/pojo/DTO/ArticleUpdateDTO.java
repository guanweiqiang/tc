package com.demo.pojo.DTO;

import lombok.Data;

@Data
public class ArticleUpdateDTO {

    private Integer id;
    private String title;
    private String content;
}
