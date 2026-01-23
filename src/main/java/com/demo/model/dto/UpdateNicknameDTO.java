package com.demo.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateNicknameDTO {

    //todo: define annotation self to check nickname
    @NotBlank(message = "昵称不能为空")
    private String nickname;
}
