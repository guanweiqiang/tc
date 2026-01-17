package com.demo.pojo.dto;

import lombok.Data;

@Data
public class CommentUserDTO {

    private Long commentId;
    private Long userId;
    private String nickname;
    private String avatarUrl;
}
