package com.demo.exceptionHandler;

import com.demo.exception.GlobalException;
import com.demo.pojo.Response;
import com.demo.pojo.ResponseCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandle {

    @ExceptionHandler(RuntimeException.class)
    public Response<Void> runtimeException(RuntimeException exception) {
        return Response.error(ResponseCode.INTERNAL_ERROR);
    }

    @ExceptionHandler(GlobalException.class)
    public Response<Void> globalExceptionHandler(GlobalException exception) {
        return Response.error(exception.getErrorCode(), exception.getMessage());
    }
}
