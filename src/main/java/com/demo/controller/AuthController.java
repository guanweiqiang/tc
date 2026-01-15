package com.demo.controller;


import com.demo.advice.BizLog;
import com.demo.pojo.DTO.EmailCodeLoginDTO;
import com.demo.pojo.DTO.EmailPwdLoginDTO;
import com.demo.pojo.DTO.LoginDTO;
import com.demo.pojo.DTO.UserRegisterDTO;
import com.demo.pojo.EmailVerifyPurpose;
import com.demo.pojo.Response;
import com.demo.pojo.VO.UserLoginVO;
import com.demo.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("auth")
public class AuthController {

    @Resource
    private AuthService service;


    @GetMapping("sendVerificationCode")
    @BizLog("send verification code by email")
    public Response<String> sendVerificationCode(@RequestParam String email, @RequestParam EmailVerifyPurpose purpose) throws MessagingException {

        service.sendVerificationCode(email, purpose);

        return Response.ok();
    }

    @PostMapping("register")
    @BizLog("register")
    public Response<Void> register(@RequestBody UserRegisterDTO userRegisterReq) {

        service.register(userRegisterReq);
        return Response.ok();
    }


    @PostMapping("login")
    @BizLog("login by username and password")
    public Response<UserLoginVO> login(@RequestBody LoginDTO userLoginDTO) {
        return Response.ok(service.login(userLoginDTO));
    }

    @PostMapping("loginByEmailPwd")
    @BizLog("login by email and password")
    public Response<UserLoginVO> loginByEmailPwd(@RequestBody EmailPwdLoginDTO emailLoginDTO) {
        return Response.ok(service.loginByEmailPWD(emailLoginDTO));
    }

    @PostMapping("loginByEmailCode")
    @BizLog("login by email and verification code")
    public Response<UserLoginVO> loginByEmailCode(@RequestBody EmailCodeLoginDTO emailCodeLoginDTO) {
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
