package com.demo.controller;


import com.demo.advice.BizLog;
import com.demo.config.FileUploadProperties;
import com.demo.model.dto.UpdateEmailDTO;
import com.demo.model.dto.UpdateNicknameDTO;
import com.demo.model.dto.UserUpdatePasswordDTO;
import com.demo.model.dto.VerifyOldEmailDTO;
import com.demo.pojo.Response;
import com.demo.pojo.UserContext;
import com.demo.model.vo.UserProfileVO;
import com.demo.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("user")
public class UserController {

    @Resource
    private UserService service;

    @Resource
    private FileUploadProperties fileUploadProperties;



    @PutMapping("changePassword")
    @BizLog("update password")
    public Response<Void> updatePassword(@Valid @RequestBody UserUpdatePasswordDTO dto) {
        Long userId = UserContext.get();
        service.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Response.ok();
    }

    @GetMapping("getAvatar")
    @BizLog("get avatar")
    public Response<String> getAvatar() {
        Long userId = UserContext.get();
        String avatar = service.getAvatar(userId);
        return Response.ok(fileUploadProperties.getAvatarUrlPrefix() + avatar);
    }

    @PostMapping("updateAvatar")
    @BizLog("update avatar")
    public Response<String> updateAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + ".png";
        Path path = Paths.get(fileUploadProperties.getAvatarPath(), filename);
        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path);
        service.updateAvatar(UserContext.get(), filename);

        return Response.ok(fileUploadProperties.getAvatarUrlPrefix() + filename);
    }

    @GetMapping("profile")
    @BizLog("get profile info")
    public Response<UserProfileVO> profile() {
        Long userId = UserContext.get();
        return Response.ok(service.getProfile(userId));
    }

    @PostMapping("updateNickname")
    @BizLog("update nickname")
    public Response<Void> updateNickname(@Valid @RequestBody UpdateNicknameDTO updateNicknameDTO) {
        Long userId = UserContext.get();
        service.updateNickname(userId, updateNicknameDTO.getNickname());
        return Response.ok();
    }

    @PostMapping("verifyOldEmail")
    @BizLog("verify old email when update email")
    public Response<String> verifyOldEmail(@Valid @RequestBody VerifyOldEmailDTO verifyOldEmailDTO) {
        String ticket = service.verifyOldEmail(verifyOldEmailDTO);
        return Response.ok(ticket);
    }


    @PostMapping("updateEmail")
    @BizLog("update email")
    public Response<Void> updateEmail(@Valid @RequestBody UpdateEmailDTO updateEmailDTO) {
        service.updateEmail(updateEmailDTO);
        return Response.ok();
    }

}
