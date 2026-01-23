package com.demo.common.exception.user;

import com.demo.common.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class ProfileUpdateException extends GlobalException {

    public ProfileUpdateException() {
        super(ResponseCode.PROFILE_UPDATE_FAIL);
    }

    public ProfileUpdateException(String message) {
        super(ResponseCode.PROFILE_UPDATE_FAIL.getCode(), message);
    }
}
