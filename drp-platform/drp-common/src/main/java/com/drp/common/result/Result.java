package com.drp.common.result;

import com.drp.common.constant.BusinessError;
import com.drp.common.constant.ResultCode;
import com.drp.common.dto.PageResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * 统一响应封装
 *
 * @author Nick
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 追踪ID
     */
    private String traceId;

    /**
     * 请求ID（用于日志追踪）
     */
    private String requestId;

    /**
     * 无参构造函数
     */
    public Result() {
    }

    /**
     * 全参构造函数
     */
    public Result(int code, String message, T data, long timestamp, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.traceId = traceId;
    }

    // ==================== Getter & Setter ====================

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    // ==================== 成功响应 ====================

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> Result<T> success(T data) {
        return success("操作成功", data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(TraceContext.getTraceId());
        return result;
    }

    /**
     * 成功响应（自定义消息，无数据）
     */
    public static <T> Result<T> success(String message) {
        return success(message, null);
    }

    // ==================== 错误响应 ====================

    /**
     * 错误响应（默认500）
     */
    public static <T> Result<T> error(String message) {
        return error(ResultCode.INTERNAL_ERROR.getCode(), message);
    }

    /**
     * 错误响应（自定义状态码）
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(TraceContext.getTraceId());
        return result;
    }

    /**
     * 错误响应（使用ResultCode）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 错误响应（使用ResultCode，自定义消息）
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(TraceContext.getTraceId());
        return result;
    }

    // ==================== 业务错误响应 ====================

    /**
     * 业务异常响应
     */
    public static <T> Result<T> businessError(BusinessError businessError) {
        Result<T> result = new Result<>();
        result.setCode(businessError.getCode());
        result.setMessage(businessError.getMessage());
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(TraceContext.getTraceId());
        return result;
    }

    // ==================== 分页响应 ====================

    /**
     * 分页成功响应
     */
    public static <T> Result<PageResponse<T>> pageSuccess(PageResponse<T> pageResponse) {
        Result<PageResponse<T>> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage("查询成功");
        result.setData(pageResponse);
        result.setTimestamp(System.currentTimeMillis());
        result.setTraceId(TraceContext.getTraceId());
        return result;
    }

    // ==================== 辅助方法 ====================

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}
