package com.demo.common.exception.user;

import com.demo.common.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class PasswordNotMatchException extends GlobalException {

    public PasswordNotMatchException() {
        super(ResponseCode.PASSWORD_NOT_MATCH);
    }

    public PasswordNotMatchException(String message) {
        super(ResponseCode.PASSWORD_NOT_MATCH.getCode(), message);
    }
}
