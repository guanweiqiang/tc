package com.demo.service.Impl;

import com.demo.exception.user.PasswordNotMatchException;
import com.demo.exception.user.UserNotExistsException;
import com.demo.mapper.UserMapper;
import com.demo.pojo.User;
import com.demo.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper mapper;

    @Override
    public void register(User user) {
        mapper.insert(user);
    }

    @Override
    public User login(User user) {
        User user1 = mapper.selectByUsername(user.getUsername());
        if (user1 == null) {
            throw new UserNotExistsException();
        }

        if (!user.getPassword().equals(user1.getPassword())) {
            throw new PasswordNotMatchException();
        }
        return user1;
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = mapper.selectById(userId);
        if (!user.getPassword().equals(oldPassword)) {
            throw new PasswordNotMatchException("密码错误");
        }

        mapper.updatePassword(userId, newPassword);
    }
}
