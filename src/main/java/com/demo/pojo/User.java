package com.demo.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {


    private Long id;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String avatarUrl;
    private Byte status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
