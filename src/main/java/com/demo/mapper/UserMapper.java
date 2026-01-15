package com.demo.mapper;


import com.demo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

}
