package com.demo.model.dto;

import com.demo.pojo.EmailVerifyPurpose;
import lombok.Data;

@Data
public class MailMessageDTO {

    private String email;
    private String code;
    private EmailVerifyPurpose purpose;
}
