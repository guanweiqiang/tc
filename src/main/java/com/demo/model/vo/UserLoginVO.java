package com.demo.model.vo;

import lombok.Data;

@Data
public class UserLoginVO {

    private Long id;
    private String username;
    private String avatarUrl;
    private String token;
}
