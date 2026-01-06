package com.demo.exception.user;

import com.demo.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class PasswordNotMatchException extends GlobalException {

    public PasswordNotMatchException() {
        super(ResponseCode.PASSWORD_NOT_MATCH);
    }

    public PasswordNotMatchException(String message) {
        super(ResponseCode.PASSWORD_NOT_MATCH.getStatusCode(), message);
    }
}
