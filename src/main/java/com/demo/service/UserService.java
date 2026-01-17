package com.demo.service;


import com.demo.pojo.dto.UpdateEmailDTO;
import com.demo.pojo.dto.VerifyOldEmailDTO;
import com.demo.pojo.User;
import com.demo.pojo.vo.UserProfileVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {



    void updatePassword(Long userId, String oldPassword, String newPassword);

    void updateAvatar(Long userId, String url);

    String getAvatar(Long userId);

    UserProfileVO getProfile(Long userId);

    void updateNickname(Long userId, String nickname);

    String verifyOldEmail(VerifyOldEmailDTO verifyOldEmailDTO);

    void updateEmail(UpdateEmailDTO updateEmailDTO);

    String getUsername(Long userId);

    String getNickname(Long userId);

    Map<Long, String> getNicknameBatch(Set<Long> userIds);

    Map<Long, User> getUserBatch(Set<Long> userIds);

    String buildAvatar(String avatar);

}
