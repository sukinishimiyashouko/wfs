package com.wbu.advice;

import com.wbu.errors.BusinessException;
import com.wbu.response.CommonResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @auther 11852
 * @create 2023/7/31
 */
@Data
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 捕获Controller中抛出的不同类型的异常，从而达到异常全局处理的目的
     */

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleBusinessException(BusinessException e){
        log.info(e.getMessage());
        return new CommonResponse<>(500,e.getMessage(),null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        FieldError fieldError = e.getBindingResult().getFieldError();
        assert fieldError != null;
        return new CommonResponse(500,fieldError.getDefaultMessage(),null);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleRuntimeException(RuntimeException e){
        return new CommonResponse<>(500,e.getMessage(),null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Object handleException(MethodArgumentNotValidException e){
        return new CommonResponse<>(500,e.getMessage(),null);
    }
}
