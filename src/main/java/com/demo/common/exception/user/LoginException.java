package com.demo.common.exception.user;

import com.demo.common.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class LoginException extends GlobalException {
    public LoginException() {
        super(ResponseCode.LOGIN_ERROR);
    }

    public LoginException(String message) {
        super(ResponseCode.LOGIN_ERROR.getCode(), message);
    }
}
