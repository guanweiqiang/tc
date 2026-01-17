package com.demo.service;

import com.demo.pojo.dto.EmailCodeLoginDTO;
import com.demo.pojo.dto.EmailPwdLoginDTO;
import com.demo.pojo.dto.LoginDTO;
import com.demo.pojo.dto.UserRegisterDTO;
import com.demo.pojo.EmailVerifyPurpose;
import com.demo.pojo.vo.UserLoginVO;
import jakarta.mail.MessagingException;

public interface AuthService {

    void sendVerificationCode(String email, EmailVerifyPurpose purpose) throws MessagingException;

    void register(UserRegisterDTO user);

    UserLoginVO login(LoginDTO user);

    void logout(String token);

    UserLoginVO loginByEmailPWD(EmailPwdLoginDTO emailLoginDTO);

    UserLoginVO loginByEmailCode(EmailCodeLoginDTO emailCodeLoginDTO);
}
