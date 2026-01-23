package com.demo.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {

    private Long id;

    private Long userId;
    private String nickname;
    private String avatar;

    private String content;

    private Long replyToId;
    private String replyToName;

    private Integer subCount;

    private LocalDateTime createdAt;
}
