package com.demo.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.demo.config.FileUploadProperties;
import com.demo.exception.GlobalException;
import com.demo.exception.user.ProfileUpdateException;
import com.demo.exception.user.PasswordNotMatchException;
import com.demo.mapper.UserMapper;
import com.demo.pojo.DTO.UpdateEmailDTO;
import com.demo.pojo.DTO.VerifyOldEmailDTO;
import com.demo.pojo.EmailVerifyPurpose;
import com.demo.pojo.User;
import com.demo.pojo.UserContext;
import com.demo.pojo.VO.UserProfileVO;
import com.demo.service.EmailVerificationService;
import com.demo.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper mapper;

    @Resource
    private FileUploadProperties fileUploadProperties;

    @Resource
    private EmailVerificationService emailVerificationService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = mapper.selectById(userId);
        if (!user.getPassword().equals(oldPassword)) {
            throw new PasswordNotMatchException("密码错误");
        }

        int i = mapper.updatePassword(userId, newPassword);
        if (i != 1) {
            throw new GlobalException("密码更新失败");
        }
    }



    @Override
    public void updateAvatar(Long userId, String url) {
        String oldAvatar = mapper.getAvatar(userId);
        int i = mapper.updateAvatar(userId, url);
        if (i != 1) {
            throw new ProfileUpdateException("头像更新失败");
        }

        //delete the old avatar when update
        if (oldAvatar != null) {
            try {
                Files.delete(Path.of(fileUploadProperties.getAvatarPath(), oldAvatar));
            } catch (IOException e) {
                log.warn("头像删除失败，请手动删除{}", oldAvatar);
            }
        }
    }

    @Override
    public String getAvatar(Long userId) {
        String avatar = mapper.getAvatar(userId);
        if (avatar == null) {
            return "default.png";
        }
        return avatar;
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = mapper.selectById(userId);
        UserProfileVO userProfileVO = new UserProfileVO();
        BeanUtil.copyProperties(user, userProfileVO);
        if (user.getAvatarUrl() == null) {
            user.setAvatarUrl("default.png");
        }
        userProfileVO.setAvatar(fileUploadProperties.getAvatarUrlPrefix() + user.getAvatarUrl());
        return userProfileVO;
    }

    @Override
    public void updateNickname(Long userId, String nickname) {
        int i = mapper.updateNickname(userId, nickname);
        if (i != 1) {
            throw new ProfileUpdateException("昵称更新失败");
        }
    }

    @Override
    public String verifyOldEmail(VerifyOldEmailDTO verifyOldEmailDTO) {
        //get the old email
        Long userId = UserContext.get();
        String email = mapper.selectById(userId).getEmail();
        emailVerificationService.verify(email, verifyOldEmailDTO.getCode(), EmailVerifyPurpose.UPDATE_EMAIL_OLD);

        //after, generate a ticket to verify new email
        //store to redis and keep only one minute
        String ticket = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "email:verify:ticket:" + userId,
                ticket,
                1,
                TimeUnit.MINUTES
        );

        return ticket;
    }

    @Override
    public void updateEmail(UpdateEmailDTO updateEmailDTO) {
        Long userId = UserContext.get();
        String email = updateEmailDTO.getEmail();
        //verify the ticket
        String key = "email:verify:ticket:" + userId;
        String storedTicket = (String)redisTemplate.opsForValue().get(key);


        if (storedTicket == null || !storedTicket.equals(updateEmailDTO.getTicket())) {
            log.warn("{}正在非法验证", email);
            throw new ProfileUpdateException("非法操作");
        }

        //verify the email code
        emailVerificationService.verify(email, updateEmailDTO.getCode(), EmailVerifyPurpose.UPDATE_EMAIL_NEW);

        //update email
        int i = mapper.updateEmail(userId, email);
        if (i != 1) {
            throw new ProfileUpdateException("更新邮箱失败");
        }

        //delete the ticket
        redisTemplate.delete(key);
    }
}
