package com.demo.pojo.dto;

import lombok.Data;

@Data
public class UserUpdatePasswordDTO {

    private String oldPassword;
    private String newPassword;
}
