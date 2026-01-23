package com.demo.common.exception.article;

import com.demo.common.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class ElasticSearchException extends GlobalException {

    public ElasticSearchException() {
        super(ResponseCode.ELASTICSEARCH_FAIL);
    }

    public ElasticSearchException(String message) {
        super(ResponseCode.ELASTICSEARCH_FAIL.getCode(), message);
    }
}
