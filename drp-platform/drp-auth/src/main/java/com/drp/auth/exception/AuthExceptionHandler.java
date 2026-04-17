package com.drp.auth.exception;

import com.drp.auth.exception.AuthException;
import com.drp.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证模块全局异常处理器
 *
 * @author Nick
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    // ==================== 认证异常处理 ====================

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthException.class)
    public Result<Void> handleAuthException(AuthException e, HttpServletRequest request) {
        log.warn("认证异常 | URI: {} | Code: {} | Message: {}",
                request.getRequestURI(), e.getCode(), e.getMessage());

        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理 Spring Security 认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("Spring Security 认证异常 | URI: {} | Type: {} | Message: {}",
                request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage());

        String message = "认证失败";
        int code = 401;

        if (e instanceof BadCredentialsException) {
            message = "用户名或密码错误";
        } else if (e instanceof DisabledException) {
            message = "用户已被禁用";
        } else if (e instanceof LockedException) {
            message = "用户已被锁定";
        }

        return Result.error(code, message);
    }

    /**
     * 处理 Spring Security 授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("授权失败 | URI: {} | Message: {}", request.getRequestURI(), e.getMessage());

        return Result.error(403, "权限不足");
    }

    // ==================== 参数校验异常处理 ====================

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";

        log.warn("参数校验失败 | Message: {}", message);

        return Result.error(400, message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        FieldError fieldError = e.getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";

        log.warn("参数绑定失败 | Message: {}", message);

        return Result.error(400, message);
    }

    // ==================== 其他异常处理 ====================

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 | URI: {} | Type: {} | Message: {}",
                request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage(), e);

        return Result.error(500, "系统异常，请稍后重试");
    }
}
