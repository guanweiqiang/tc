package com.demo.common.exceptionHandler;

import com.demo.common.exception.GlobalException;
import com.demo.pojo.Response;
import com.demo.pojo.ResponseCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandle {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> methodArgumentNotValid(MethodArgumentNotValidException exception) {
        String msg = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return Response.error(ResponseCode.VALIDATION_ERROR, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Response<Void> constraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().iterator().next().getMessage();
        return Response.error(ResponseCode.VALIDATION_ERROR, msg);
    }

    @ExceptionHandler(RuntimeException.class)
    public Response<Void> runtime(RuntimeException exception) {
        return Response.error(ResponseCode.INTERNAL_ERROR, exception.getMessage());
    }

    @ExceptionHandler(GlobalException.class)
    public Response<Void> global(GlobalException exception) {
        return Response.error(exception.getErrorCode(), exception.getMessage());
    }
}
