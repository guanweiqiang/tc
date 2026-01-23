package com.demo.model.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class VerifyOldEmailDTO {

    @NotBlank(message = "验证码不能为空")
    @Length(min = 6, max = 6, message = "验证码长度不正确")
    private String code;
}
