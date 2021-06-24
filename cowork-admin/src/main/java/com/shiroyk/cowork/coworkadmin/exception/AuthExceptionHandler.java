package com.shiroyk.cowork.coworkadmin.exception;

import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(BindException.class)
    public APIResponse<Object> handleInvalidParameterException(BindException e) {
        BindingResult result = e.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();
        if (!fieldErrors.isEmpty())
            return APIResponse.badRequest(fieldErrors.get(0).getDefaultMessage());
        return APIResponse.badRequest("请求参数错误！");
    }

}
