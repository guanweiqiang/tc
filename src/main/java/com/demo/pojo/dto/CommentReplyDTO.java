package com.demo.pojo.dto;

import lombok.Data;

@Data
public class CommentReplyDTO {

    private Long articleId;
    private Long rootId;
    private Long replyToId;
    private String content;
}
