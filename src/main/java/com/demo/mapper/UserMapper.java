package com.demo.mapper;


import com.demo.pojo.User;
import com.demo.model.dto.UserIdNicknameDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserMapper {

    int insert(User user);

    User selectByUsername(String username);

    User selectById(Long id);

    User selectByEmail(String email);

    int updatePassword(@Param("id") Long id, @Param("newPassword") String newPassword);

    int updateAvatar(@Param("id") Long userId, @Param("url") String url);

    String getAvatar(Long id);

    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    int updateEmail(@Param("id") Long id, @Param("email") String email);

    String getUsername(Long userId);

    String getNickname(Long userId);

    List<UserIdNicknameDTO> getNicknameBatch(Set<Long> userIds);

    List<User> getUserBatch(@Param("userIds") Set<Long> userIds);

}
