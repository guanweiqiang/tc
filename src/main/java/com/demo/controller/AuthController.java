package com.demo.controller;


import com.demo.advice.BizLog;
import com.demo.model.dto.EmailCodeLoginDTO;
import com.demo.model.dto.EmailPwdLoginDTO;
import com.demo.model.dto.LoginDTO;
import com.demo.model.dto.UserRegisterDTO;
import com.demo.pojo.EmailVerifyPurpose;
import com.demo.pojo.Response;
import com.demo.model.vo.UserLoginVO;
import com.demo.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("auth")
@Validated
public class AuthController {

    @Resource
    private AuthService service;


    @GetMapping("sendVerificationCode")
    @BizLog("send verification code by email")
    public Response<String> sendVerificationCode(
            @RequestParam
            @Email(message = "邮件格式不正确")
            @NotBlank(message = "邮件不能为空")
            String email,
            @RequestParam
            @NotNull(message = "发邮件目的不能为空")
            EmailVerifyPurpose purpose) throws MessagingException {

        service.sendVerificationCode(email, purpose);

        return Response.ok();
    }

    @PostMapping("register")
    @BizLog("register")
    public Response<Void> register(@Valid @RequestBody UserRegisterDTO userRegisterReq) {

        service.register(userRegisterReq);
        return Response.ok();
    }


    @PostMapping("login")
    @BizLog("login by username and password")
    public Response<UserLoginVO> login(@Valid @RequestBody LoginDTO userLoginDTO) {
        return Response.ok(service.login(userLoginDTO));
    }

    @PostMapping("loginByEmailPwd")
    @BizLog("login by email and password")
    public Response<UserLoginVO> loginByEmailPwd(@Valid @RequestBody EmailPwdLoginDTO emailLoginDTO) {
        return Response.ok(service.loginByEmailPWD(emailLoginDTO));
    }

    @PostMapping("loginByEmailCode")
    @BizLog("login by email and verification code")
    public Response<UserLoginVO> loginByEmailCode(@Valid @RequestBody EmailCodeLoginDTO emailCodeLoginDTO) {
        return  Response.ok(service.loginByEmailCode(emailCodeLoginDTO));
    }

    @PostMapping("logout")
    @BizLog("logout")
    public Response<Void> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(7);
        service.logout(token);
        return Response.ok();
    }

}
