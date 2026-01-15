package com.demo.exception.user;

import com.demo.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class UserAlreadyExistsException extends GlobalException {

    public UserAlreadyExistsException() {
        super(ResponseCode.USER_ALREADY_EXISTS);
    }

    public UserAlreadyExistsException(String message) {
        super(ResponseCode.USER_ALREADY_EXISTS.getCode(), message);
    }
}
