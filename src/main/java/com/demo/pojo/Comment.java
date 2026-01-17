package com.demo.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {

    private Long id;
    private Long articleId;
    private Long rootId;
    private Long replyToId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;

}
