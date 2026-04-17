package com.drp.auth.exception;

import com.drp.common.constant.ResultCode;

/**
 * 认证异常
 * <p>
 * 用于处理认证授权过程中的异常情况
 *
 * @author Nick
 */
public class AuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误类型
     */
    private final String errorType;

    // ==================== 构造函数 ====================

    /**
     * 使用 ResultCode 构造认证异常
     */
    public AuthException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.errorType = "AUTH";
    }

    /**
     * 使用 ResultCode 和自定义消息构造认证异常
     */
    public AuthException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.errorType = "AUTH";
    }

    /**
     * 使用错误码和消息构造认证异常
     */
    public AuthException(int code, String message) {
        super(message);
        this.code = code;
        this.errorType = "AUTH";
    }

    /**
     * 使用消息构造认证异常
     */
    public AuthException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
        this.errorType = "AUTH";
    }

    // ==================== Getter ====================

    public int getCode() {
        return code;
    }

    public String getErrorType() {
        return errorType;
    }

    /**
     * 获取错误描述
     */
    public String getErrorDescription() {
        return String.format("[%s] %s", code, getMessage());
    }
}
