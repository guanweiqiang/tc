package com.demo.pojo.dto;

import lombok.Data;

import java.time.LocalDate;


@Data
public class ArticleSearchListDTO {

    private String author;
    private String keyword;
    private LocalDate from;
    private LocalDate to;
    private Integer page;
    private Integer size;

}
