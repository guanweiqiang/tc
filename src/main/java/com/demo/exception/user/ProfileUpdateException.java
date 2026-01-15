package com.demo.exception.user;

import com.demo.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class ProfileUpdateException extends GlobalException {

    public ProfileUpdateException() {
        super(ResponseCode.PROFILE_UPDATE_FAIL);
    }

    public ProfileUpdateException(String message) {
        super(ResponseCode.PROFILE_UPDATE_FAIL.getCode(), message);
    }
}
