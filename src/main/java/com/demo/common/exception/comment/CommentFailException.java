package com.demo.common.exception.comment;

import com.demo.common.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class CommentFailException extends GlobalException {

    public CommentFailException() {
        super(ResponseCode.COMMENT_FAIL);
    }

    public CommentFailException(String message) {
        super(ResponseCode.COMMENT_FAIL.getCode(), message);
    }
}
