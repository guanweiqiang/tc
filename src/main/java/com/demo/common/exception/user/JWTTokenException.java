package com.demo.common.exception.user;

import com.demo.common.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class JWTTokenException extends GlobalException {

    public JWTTokenException() {
        super(ResponseCode.JWT_TOKEN_ERROR);
    }

    public JWTTokenException(String message) {
        super(ResponseCode.JWT_TOKEN_ERROR.getCode(), message);
    }
}
