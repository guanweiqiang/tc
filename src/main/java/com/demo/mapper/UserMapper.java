package com.demo.mapper;


import com.demo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    void insert(User user);

    User selectByUsername(String username);

    User selectById(Long id);

    void updatePassword(@Param("id") Long id, @Param("newPassword") String newPassword);

}
