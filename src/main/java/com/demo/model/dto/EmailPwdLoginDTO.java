package com.demo.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmailPwdLoginDTO {

    @Email(message = "邮箱格式不正确")
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 32, message = "密码必须在8~32位")
    private String password;
}
