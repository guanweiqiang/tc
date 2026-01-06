package com.demo.pojo;

import lombok.Data;
import lombok.Getter;


@Getter
public enum ResponseCode {


    SUCCESS(200, "请求成功", true),
    BAD_REQUEST(400, "请求参数错误", false),
    UNAUTHORIZED(401, "未授权", false),
    FORBIDDEN(403, "无权限访问", false),
    NOT_FOUND(404, "资源不存在", false),
    VALIDATION_ERROR(422, "参数校验失败",false),
    INTERNAL_ERROR(500, "服务器内部错误", false),

    //user
    USER_NOT_EXISTS(1001, "用户不存在", false),
    PASSWORD_NOT_MATCH(1002, "密码与用户名不匹配", false),
    JWT_TOKEN_ERROR(1003, "JWT token错误", false),
    LOGIN_ERROR(1004, "登陆错误", false);





    private final Integer statusCode;
    private final String message;
    private final Boolean isSuccess;

    ResponseCode(Integer code, String message, Boolean isSuccess) {
        this.statusCode = code;
        this.message = message;
        this.isSuccess = isSuccess;
    }
}
