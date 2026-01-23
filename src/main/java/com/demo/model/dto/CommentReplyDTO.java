package com.demo.model.dto;

import com.demo.common.validation.annoatation.NoForbiddenWord;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CommentReplyDTO {

    @NotNull(message = "文章id不能为空")
    @Positive(message = "文章id必须为正数")
    private Long articleId;

    @NotNull(message = "rootId不能为空")
    @Positive(message = "rootId必须为正数")
    private Long rootId;

    @NotNull(message = "replyToId不能为空")
    @Positive(message = "replyToId必须为正数")
    private Long replyToId;

    @NoForbiddenWord
    private String content;
}
