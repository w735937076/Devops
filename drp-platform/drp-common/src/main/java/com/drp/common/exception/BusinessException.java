package com.drp.common.exception;

import com.drp.common.constant.BusinessError;
import com.drp.common.constant.ResultCode;

/**
 * 业务异常
 * <p>
 * 用于处理业务逻辑中的异常情况
 *
 * @author Nick
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误类型（用于区分不同类别的业务异常）
     */
    private final String errorType;

    /**
     * 是否已记录日志（防止重复记录）
     */
    private boolean logged = false;

    // ==================== 基于 ResultCode 的构造方法 ====================

    /**
     * 使用 ResultCode 构造业务异常
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.errorType = "BUSINESS";
    }

    /**
     * 使用 ResultCode 和自定义消息构造业务异常
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.errorType = "BUSINESS";
    }

    /**
     * 使用 ResultCode 和Throwable构造业务异常
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
        this.errorType = "BUSINESS";
    }

    // ==================== 基于 BusinessError 的构造方法 ====================

    /**
     * 使用 BusinessError 构造业务异常
     */
    public BusinessException(BusinessError businessError) {
        super(businessError.getMessage());
        this.code = businessError.getCode();
        this.errorType = businessError.getI18nKey();
    }

    /**
     * 使用 BusinessError 和自定义消息构造业务异常
     */
    public BusinessException(BusinessError businessError, String message) {
        super(message);
        this.code = businessError.getCode();
        this.errorType = businessError.getI18nKey();
    }

    // ==================== 基于错误码和消息的构造方法 ====================

    /**
     * 使用错误码和消息构造业务异常
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorType = "BUSINESS";
    }

    /**
     * 使用错误码、消息和Throwable构造业务异常
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.errorType = "BUSINESS";
    }

    // ==================== 基于消息的构造方法 ====================

    /**
     * 仅使用消息构造业务异常（错误码默认为500）
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
        this.errorType = "BUSINESS";
    }

    /**
     * 仅使用消息和Throwable构造业务异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
        this.errorType = "BUSINESS";
    }

    // ==================== Getter ====================

    /**
     * 获取错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取错误类型
     */
    public String getErrorType() {
        return errorType;
    }

    // ==================== 辅助方法 ====================

    /**
     * 标记为已记录日志
     */
    public BusinessException markAsLogged() {
        this.logged = true;
        return this;
    }

    /**
     * 判断是否已记录日志
     */
    public boolean isLogged() {
        return logged;
    }

    /**
     * 获取错误描述
     */
    public String getErrorDescription() {
        return String.format("[%s] %s", code, getMessage());
    }

    /**
     * 转换为 ResultCode
     */
    public ResultCode toResultCode() {
        for (ResultCode rc : ResultCode.values()) {
            if (rc.getCode() == this.code) {
                return rc;
            }
        }
        return ResultCode.INTERNAL_ERROR;
    }
}
