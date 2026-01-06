package com.demo.exception.user;

import com.demo.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class LoginException extends GlobalException {
    public LoginException() {
        super(ResponseCode.LOGIN_ERROR);
    }

    public LoginException(String message) {
        super(ResponseCode.LOGIN_ERROR.getStatusCode(), message);
    }
}
