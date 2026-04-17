package com.drp.common.exception;

import com.drp.common.constant.ResultCode;
import com.drp.common.result.Result;
import com.drp.common.result.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author Nick
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== 业务异常处理 ====================

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        // 防止重复记录日志
        if (!e.isLogged()) {
            log.warn("业务异常 | URI: {} | Code: {} | Message: {}",
                    request.getRequestURI(), e.getCode(), e.getMessage());
            e.markAsLogged();
        }
        return Result.error(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验异常处理 ====================

    /**
     * 处理参数校验异常（@Valid 校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        log.warn("参数校验失败 | Message: {}", message);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理约束违反异常（@Validated 校验失败）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败 | Message: {}", message);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理绑定异常（表单参数绑定失败）
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        FieldError fieldError = e.getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.warn("参数绑定失败 | Message: {}", message);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = "缺少必需参数: " + e.getParameterName();
        log.warn("缺少请求参数 | Parameter: {}", e.getParameterName());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("参数 %s 类型不匹配，期望类型: %s",
                e.getName(), Objects.requireNonNull(e.getRequiredType()).getSimpleName());
        log.warn("参数类型不匹配 | Name: {} | Expected: {}", e.getName(), e.getRequiredType());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    // ==================== HTTP 相关异常处理 ====================

    /**
     * 处理请求体解析异常（JSON格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败 | Message: {}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误或为空");
    }

    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String message = String.format("不支持 %s 请求方法", e.getMethod());
        log.warn("请求方法不支持 | Method: {}", e.getMethod());
        return Result.error(ResultCode.METHOD_NOT_ALLOWED.getCode(), message);
    }

    /**
     * 处理媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        String message = String.format("不支持 %s 媒体类型", e.getContentType());
        log.warn("媒体类型不支持 | ContentType: {}", e.getContentType());
        return Result.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), message);
    }

    /**
     * 处理缺少路径变量异常
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public Result<Void> handleMissingPathVariableException(MissingPathVariableException e) {
        String message = String.format("缺少路径变量: %s", e.getVariableName());
        log.warn("缺少路径变量 | Variable: {}", e.getVariableName());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("未找到处理器 | Path: {} | Method: {}", e.getRequestURL(), e.getHttpMethod());
        return Result.error(ResultCode.NOT_FOUND.getCode(), "接口不存在: " + e.getRequestURL());
    }

    // ==================== 其他异常处理 ====================

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        // 记录异常日志
        log.error("系统异常 | URI: {} | TraceId: {} | Error: {}",
                request.getRequestURI(),
                TraceContext.getTraceId(),
                e.getClass().getName(),
                e);

        // 根据异常类型返回不同的错误信息
        String message = getExceptionMessage(e);
        return Result.error(ResultCode.INTERNAL_ERROR.getCode(), message);
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取异常的友好消息
     */
    private String getExceptionMessage(Exception e) {
        // 如果是业务异常，直接返回业务消息
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            // 避免暴露内部实现细节
            if (e.getMessage().contains("at ") || e.getMessage().contains("\n")) {
                return "系统处理异常，请稍后重试";
            }
            return e.getMessage();
        }
        return "系统异常，请稍后重试";
    }
}
