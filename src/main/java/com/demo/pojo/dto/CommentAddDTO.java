package com.demo.pojo.dto;

import lombok.Data;

@Data
public class CommentAddDTO {

    private Long articleId;
    private String content;

}
