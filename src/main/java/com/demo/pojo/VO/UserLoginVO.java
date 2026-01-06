package com.demo.pojo.VO;

import lombok.Data;

@Data
public class UserLoginVO {

    private Long id;
    private String username;
    private String avatarUrl;
    private String token;
}
