package com.demo.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CommentAddDTO {

    @NotNull(message = "文章id不能为空")
    @Positive(message = "文章id必须为正数")
    private Long articleId;

    //self define to check
    private String content;

}
