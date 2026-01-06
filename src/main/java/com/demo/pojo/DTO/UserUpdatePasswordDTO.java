package com.demo.pojo.DTO;

import lombok.Data;

@Data
public class UserUpdatePasswordDTO {

    private String oldPassword;
    private String newPassword;
}
