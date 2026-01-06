package com.demo.service;

import com.demo.pojo.User;

public interface UserService {


    void register(User user);

    User login(User user);

    void updatePassword(Long userId, String oldPassword, String newPassword);
}
