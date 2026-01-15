package com.demo.service;

import com.demo.pojo.DTO.EmailCodeLoginDTO;
import com.demo.pojo.DTO.EmailPwdLoginDTO;
import com.demo.pojo.DTO.LoginDTO;
import com.demo.pojo.DTO.UserRegisterDTO;
import com.demo.pojo.EmailVerifyPurpose;
import com.demo.pojo.VO.UserLoginVO;
import jakarta.mail.MessagingException;

public interface AuthService {

    void sendVerificationCode(String email, EmailVerifyPurpose purpose) throws MessagingException;

    void register(UserRegisterDTO user);

    UserLoginVO login(LoginDTO user);

    void logout(String token);

    UserLoginVO loginByEmailPWD(EmailPwdLoginDTO emailLoginDTO);

    UserLoginVO loginByEmailCode(EmailCodeLoginDTO emailCodeLoginDTO);
}
