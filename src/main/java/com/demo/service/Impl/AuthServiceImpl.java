package com.demo.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.demo.common.exception.user.PasswordNotMatchException;
import com.demo.common.exception.user.UserAlreadyExistsException;
import com.demo.common.exception.user.UserNotExistsException;
import com.demo.mapper.UserMapper;
import com.demo.model.dto.EmailCodeLoginDTO;
import com.demo.model.dto.EmailPwdLoginDTO;
import com.demo.model.dto.LoginDTO;
import com.demo.model.dto.UserRegisterDTO;
import com.demo.pojo.EmailVerifyPurpose;
import com.demo.pojo.User;
import com.demo.pojo.UserContext;
import com.demo.model.vo.UserLoginVO;
import com.demo.service.AuthService;
import com.demo.service.EmailVerificationService;
import com.demo.common.util.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserMapper mapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private EmailVerificationService emailVerificationService;


    //todo: use rabbitmq to send email async
    @Override
    public void sendVerificationCode(String email, EmailVerifyPurpose purpose) throws MessagingException {
        switch (purpose)  {
            case LOGIN, RESET_PASSWORD -> {
                if (mapper.selectByEmail(email) == null) {
                    throw new UserNotExistsException("该邮箱没有注册");
                }
            }
            case REGISTER-> {
                if (mapper.selectByEmail(email) != null) {
                    throw new UserAlreadyExistsException("该邮箱已经注册");
                }
            }
        }

        emailVerificationService.sendMail(email, purpose);
    }



    @Override
    public void register(UserRegisterDTO userRegisterDTO) {

        emailVerificationService.verify(
                userRegisterDTO.getEmail(),
                userRegisterDTO.getCode(),
                EmailVerifyPurpose.REGISTER
        );

        //check the username and email to prevent repeat register
        //username and email and not allowed the same
        if (mapper.selectByUsername(userRegisterDTO.getUsername()) != null) {
            throw new UserAlreadyExistsException("用户名已存在");
        }

        //recheck the email
        if (mapper.selectByEmail(userRegisterDTO.getEmail()) != null) {
            throw new UserAlreadyExistsException("该邮箱已被注册");
        }

        //if success, insert the user to db
        User user = new User();
        BeanUtil.copyProperties(userRegisterDTO, user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            mapper.insert(user);
        } catch (DuplicateKeyException e) {
            //use db unique constraint to recheck
            throw new UserAlreadyExistsException("用户名或邮箱已存在");
        }

        //after success, delete the code to prevent abuse
        redisTemplate.delete("email:verify:REGISTER:" + userRegisterDTO.getEmail());

    }


    @Override
    public UserLoginVO login(LoginDTO user) {
        User login = mapper.selectByUsername(user.getUsername());
        if (login == null) {
            throw new UserNotExistsException();
        }

        if (!passwordEncoder.matches(user.getPassword(), login.getPassword())) {
            throw new PasswordNotMatchException();
        }
        return handleLoginSuccess(login);
    }


    @Override
    public void logout(String token) {

        Date expiration = JWTUtil.parseExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();

        //after logout, put the token to blacklist for abuse of token
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        if (ttl > 0) {
            ops.set("jwt:blacklist:" + token, 1, ttl, TimeUnit.MILLISECONDS);
        }
        Long userId = UserContext.get();
        ops.getAndDelete("login:active:" + userId);
    }

    @Override
    public UserLoginVO loginByEmailPWD(EmailPwdLoginDTO emailLoginDTO) {
        User user = mapper.selectByEmail(emailLoginDTO.getEmail());

        //check whether the user is null
        if (user == null) {
            throw new UserNotExistsException("该邮箱未注册");
        }

        //compare the password
        if (!passwordEncoder.matches(emailLoginDTO.getPassword(), user.getPassword())) {
            throw new PasswordNotMatchException("密码错误");
        }

        return handleLoginSuccess(user);

    }

    private UserLoginVO handleLoginSuccess(User user) {
        UserLoginVO userVO = new UserLoginVO();
        BeanUtil.copyProperties(user, userVO);

        String token = JWTUtil.generateJWT(user);
        userVO.setToken(token);

        //set the value to jti to keep only one user login at the same time
        redisTemplate.opsForValue().set(
                "login:active:" + user.getId(),
                JWTUtil.parseJti(token),
                JWTUtil.KEEPALIVE_TIME,
                TimeUnit.MILLISECONDS);
        return userVO;
    }

    @Override
    public UserLoginVO loginByEmailCode(EmailCodeLoginDTO emailCodeLoginDTO) {
        String email = emailCodeLoginDTO.getEmail();
        User user = mapper.selectByEmail(email);
        if (user == null) {
            throw new UserNotExistsException("该邮箱还未注册");
        }

        emailVerificationService.verify(email, emailCodeLoginDTO.getCode(), EmailVerifyPurpose.LOGIN);
        //delete the key to prevent abuse
        redisTemplate.delete("email:verify:LOGIN:" + email);
        return handleLoginSuccess(user);
    }




}
