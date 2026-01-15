package com.demo.pojo;


import lombok.Getter;

@Getter
public enum EmailVerifyPurpose {
    REGISTER,
    LOGIN,
    RESET_PASSWORD,
    UPDATE_EMAIL_OLD,
    UPDATE_EMAIL_NEW
}
