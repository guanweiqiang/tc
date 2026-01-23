package com.demo.common.exception;

import com.demo.pojo.ResponseCode;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException{

    private final Integer errorCode;
    private final String message;

    public GlobalException() {
        this.errorCode = ResponseCode.INTERNAL_ERROR.getCode();
        this.message = ResponseCode.INTERNAL_ERROR.getMessage();
    }

    public GlobalException(ResponseCode code) {
        this.errorCode = code.getCode();
        this.message = code.getMessage();
    }

    public GlobalException(String message) {
        this.errorCode = ResponseCode.INTERNAL_ERROR.getCode();
        this.message = message;
    }

    public GlobalException(Integer errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }


}
