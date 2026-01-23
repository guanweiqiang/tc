package com.demo.common.exception.auth;

import com.demo.common.exception.GlobalException;
import com.demo.pojo.ResponseCode;


public class VerificationCodeException extends GlobalException {

    public VerificationCodeException() {
        super(ResponseCode.VERIFICATION_CODE_ERROR);
    }

    public VerificationCodeException(String message) {
        super(ResponseCode.VERIFICATION_CODE_ERROR.getCode(), message);
    }
}
