package com.demo.pojo.dto;

import lombok.Data;

@Data
public class UpdateEmailDTO {

    private String email;
    private String code;
    private String ticket;
}
