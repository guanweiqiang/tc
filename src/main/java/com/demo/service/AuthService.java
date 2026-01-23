package com.demo.service;

import com.demo.model.dto.EmailCodeLoginDTO;
import com.demo.model.dto.EmailPwdLoginDTO;
import com.demo.model.dto.LoginDTO;
import com.demo.model.dto.UserRegisterDTO;
import com.demo.pojo.EmailVerifyPurpose;
import com.demo.model.vo.UserLoginVO;
import jakarta.mail.MessagingException;

public interface AuthService {

    void sendVerificationCode(String email, EmailVerifyPurpose purpose) throws MessagingException;

    void register(UserRegisterDTO user);

    UserLoginVO login(LoginDTO user);

    void logout(String token);

    UserLoginVO loginByEmailPWD(EmailPwdLoginDTO emailLoginDTO);

    UserLoginVO loginByEmailCode(EmailCodeLoginDTO emailCodeLoginDTO);
}
