package com.demo.model.dto;

import com.demo.common.validation.annoatation.NoForbiddenWord;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleAddDTO {

    @NotBlank(message = "标题不能为空白")
    @NotNull(message = "标题不能为空")
    @NoForbiddenWord
    private String title;

    @NoForbiddenWord
    private String content;

}
