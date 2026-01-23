package com.demo.service;


import com.demo.model.dto.UpdateEmailDTO;
import com.demo.model.dto.VerifyOldEmailDTO;
import com.demo.pojo.User;
import com.demo.model.vo.UserProfileVO;

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
