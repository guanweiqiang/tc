package com.demo.exception.article;

import com.demo.exception.GlobalException;
import com.demo.pojo.ResponseCode;

public class ElasticSearchException extends GlobalException {

    public ElasticSearchException() {
        super(ResponseCode.ELASTICSEARCH_FAIL);
    }

    public ElasticSearchException(String message) {
        super(ResponseCode.ELASTICSEARCH_FAIL.getCode(), message);
    }
}
