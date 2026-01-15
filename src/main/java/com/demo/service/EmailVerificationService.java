package com.demo.service;

import com.demo.pojo.EmailVerifyPurpose;

public interface EmailVerificationService {

    void sendMail(String email, EmailVerifyPurpose purpose);

    void verify(String email, String code, EmailVerifyPurpose purpose);
}
