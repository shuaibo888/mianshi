package com.example.miniats.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException validException) {
            FieldError fieldError = validException.getBindingResult().getFieldError();
            if (fieldError != null) {
                return ApiResponse.error(400, fieldError.getDefaultMessage());
            }
        }
        if (ex instanceof ConstraintViolationException violationException
                && !violationException.getConstraintViolations().isEmpty()) {
            String message = violationException.getConstraintViolations().iterator().next().getMessage();
            return ApiResponse.error(400, message);
        }
        return ApiResponse.error(400, "参数校验失败");
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleBadCredentials() {
        return ApiResponse.error(401, "账号或密码错误");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnexpected(Exception ex) {
        return ApiResponse.error(500, "服务器异常");
    }
}
