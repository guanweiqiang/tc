package com.demo.controller;


import cn.hutool.core.bean.BeanUtil;
import com.demo.exception.GlobalException;
import com.demo.pojo.DTO.UserLoginDTO;
import com.demo.pojo.DTO.UserRegisterDTO;
import com.demo.pojo.DTO.UserUpdatePasswordDTO;
import com.demo.pojo.Response;
import com.demo.pojo.User;
import com.demo.pojo.UserContext;
import com.demo.pojo.VO.UserLoginVO;
import com.demo.service.UserService;
import com.demo.util.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private UserService service;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("register")
    public Response<Void> register(@RequestBody UserRegisterDTO userRegisterReq) {
        User user = new User();
        BeanUtil.copyProperties(userRegisterReq, user);
        service.register(user);

        return Response.ok();
    }

    @PostMapping("login")
    public Response<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        User user = new User();
        BeanUtil.copyProperties(userLoginDTO, user);
        User login = service.login(user);

        UserLoginVO userVO = new UserLoginVO();
        BeanUtil.copyProperties(login, userVO);

        String token = JWTUtil.generateJWT(login);
        userVO.setToken(token);
        redisTemplate.opsForValue().set("login:active:" + login.getId(), JWTUtil.parseJti(token), JWTUtil.KEEPALIVE_TIME, TimeUnit.MILLISECONDS);
        return Response.ok(userVO);
    }

    @PutMapping("updatePassword")
    public Response<Void> updatePassword(@RequestBody UserUpdatePasswordDTO dto) {
        Long userId = UserContext.get();
        service.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Response.ok();
    }

    @PutMapping("updateAvatar")
    public Response<Void> updateAvatar(MultipartFile file) {
        try {
            file.transferTo(new File("./avatar/" + UserContext.get() + "-" + file.getName()));
        } catch (IOException e) {
            throw new GlobalException("头像上传失败");
        }

        return Response.ok();

    }

    @PostMapping("logout")
    public Response<Void> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        String token = authorization.substring(7);

        Date expiration = JWTUtil.parseExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        if (ttl > 0) {
            ops.set("jwt:blacklist:" + token, 1, ttl, TimeUnit.MILLISECONDS);
        }
        Long userId = UserContext.get();
        ops.getAndDelete("login:active:" + userId);

        return Response.ok();
    }

}
