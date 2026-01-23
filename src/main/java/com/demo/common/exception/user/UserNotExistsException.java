package com.demo.common.exception.user;

import com.demo.common.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class UserNotExistsException extends GlobalException {

    public UserNotExistsException() {
        super(ResponseCode.USER_NOT_EXISTS);
    }

    public UserNotExistsException(String message) {
        super(ResponseCode.USER_NOT_EXISTS.getCode(), message);
    }
}
