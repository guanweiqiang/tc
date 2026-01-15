package com.demo.pojo;

import com.demo.util.TimeUtil;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Response<T> {

    private Integer code;
    private String message;
    private Boolean isSuccess;
    private T data;
    private String traceId;
    private String timeStamp;

    private Response() {

    }

    private Response(ResponseCode code, T data) {
        this.code = code.getCode();
        this.message = code.getMessage();
        this.isSuccess = code.getIsSuccess();
        this.data = data;
        this.traceId = UUID.randomUUID().toString().replace("-", "");
        this.timeStamp = LocalDateTime.now().format(TimeUtil.DEFAULT_DATE_TIME_FORMATTER);
    }

    private Response(ResponseCode code) {
        this(code, null);
    }


    public static <A> Response<A> ok() {
        return new Response<A>(ResponseCode.SUCCESS);
    }

    public static <A> Response<A> ok(A data) {
        return new Response<A>(ResponseCode.SUCCESS, data);
    }

    public static <A> Response<A> error() {
        return new Response<>(ResponseCode.INTERNAL_ERROR);
    }

    public static <A> Response<A> error(ResponseCode code) {
        return new Response<>(code);
    }

    public static <A> Response<A> error(A data) {
        return new Response<>(ResponseCode.VALIDATION_ERROR, data);
    }

    public static <A> Response<A> error(ResponseCode code, A data) {
        return new Response<>(code, data);
    }

    public static <A> Response<A> error(Integer code, String message) {
        Response<A> res = error();
        res.code = code;
        res.message = message;
        return res;
    }
}
