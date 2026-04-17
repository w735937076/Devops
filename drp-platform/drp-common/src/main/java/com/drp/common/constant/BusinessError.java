package com.drp.common.constant;

/**
 * 业务错误码（用于更细粒度的业务错误描述）
 * <p>
 * 与 ResultCode 的区别：
 * - ResultCode: HTTP状态码 + 通用业务错误码，用于构建统一的错误响应结构
 * - BusinessError: 具体的业务错误场景，包含错误码和国际化消息key
 *
 * @author Nick
 */
public enum BusinessError {

    // ==================== 通用错误 ====================
    UNKNOWN_ERROR("未知错误", "error.unknown"),
    INVALID_PARAMETER("参数无效", "error.invalid_parameter"),
    DATA_NOT_FOUND("数据不存在", "error.data_not_found"),
    DATA_ALREADY_EXISTS("数据已存在", "error.data_already_exists"),
    DATA_UPDATE_FAILED("数据更新失败", "error.data_update_failed"),
    DATA_DELETE_FAILED("数据删除失败", "error.data_delete_failed"),
    OPERATION_NOT_ALLOWED("操作不允许", "error.operation_not_allowed"),
    RESOURCE_IN_USE("资源正在使用中", "error.resource_in_use"),

    // ==================== 认证相关 ====================
    LOGIN_FAILED("登录失败", "auth.login.failed"),
    LOGOUT_FAILED("登出失败", "auth.logout.failed"),
    ACCOUNT_DISABLED("账户已被禁用", "auth.account.disabled"),
    ACCOUNT_LOCKED("账户已被锁定", "auth.account.locked"),
    SESSION_EXPIRED("会话已过期", "auth.session.expired"),
    CAPTCHA_REQUIRED("需要验证码", "auth.captcha.required"),
    PASSWORD_CHANGE_REQUIRED("需要修改密码", "auth.password.change_required"),

    // ==================== 权限相关 ====================
    ACCESS_DENIED("访问被拒绝", "permission.access_denied"),
    INSUFFICIENT_PERMISSIONS("权限不足", "permission.insufficient"),
    RESOURCE_ACCESS_DENIED("资源访问被拒绝", "permission.resource_access_denied"),

    // ==================== 第三方服务错误 ====================
    THIRD_PARTY_SERVICE_ERROR("第三方服务错误", "third_party.service_error"),
    THIRD_PARTY_SERVICE_TIMEOUT("第三方服务超时", "third_party.service_timeout"),
    THIRD_PARTY_SERVICE_UNAVAILABLE("第三方服务不可用", "third_party.service_unavailable");

    /**
     * 错误描述
     */
    private final String message;

    /**
     * 国际化消息Key
     */
    private final String i18nKey;

    /**
     * 构造函数
     */
    BusinessError(String message, String i18nKey) {
        this.message = message;
        this.i18nKey = i18nKey;
    }

    /**
     * 获取消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取国际化消息Key
     */
    public String getI18nKey() {
        return i18nKey;
    }

    /**
     * 获取错误码（枚举序号 + 基础偏移量）
     */
    public int getCode() {
        return 10000 + this.ordinal();
    }
}
