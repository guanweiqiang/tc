package com.demo.model.dto;

import com.demo.common.validation.annoatation.NoForbiddenWord;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ArticleUpdateDTO {


    @NotNull(message = "文章id不能为空")
    @Positive(message = "文章id必须为正数")
    private Long id;

    @NoForbiddenWord
    private String title;

    @NoForbiddenWord
    private String content;
}
