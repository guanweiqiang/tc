package com.demo.service;


import com.demo.pojo.DTO.UpdateEmailDTO;
import com.demo.pojo.DTO.VerifyOldEmailDTO;
import com.demo.pojo.VO.UserProfileVO;

public interface UserService {



    void updatePassword(Long userId, String oldPassword, String newPassword);

    void updateAvatar(Long userId, String url);

    String getAvatar(Long userId);

    UserProfileVO getProfile(Long userId);

    void updateNickname(Long userId, String nickname);

    String verifyOldEmail(VerifyOldEmailDTO verifyOldEmailDTO);

    void updateEmail(UpdateEmailDTO updateEmailDTO);

}
