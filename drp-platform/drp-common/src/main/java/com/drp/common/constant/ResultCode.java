package com.drp.common.constant;

/**
 * 通用响应码
 * <p>
 * 规则：
 * - 2xx: 成功
 * - 4xx: 客户端错误
 * - 5xx: 服务端错误
 * - 1000+: 业务错误码（按模块划分）
 *
 * @author Nick
 */
public enum ResultCode {

    // ==================== 成功 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 客户端错误 ====================
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    REQUEST_TIMEOUT(408, "请求超时"),

    // ==================== 服务端错误 ====================
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // ==================== 业务错误码 - 认证模块 (1000+) ====================
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_PASSWORD_ERROR(1002, "用户名或密码错误"),
    TOKEN_EXPIRED(1003, "Token已过期"),
    TOKEN_INVALID(1004, "Token无效"),
    TOKEN_MALFORMED(1005, "Token格式错误"),
    USER_DISABLED(1006, "用户已被禁用"),
    USER_LOCKED(1007, "用户已被锁定"),
    PASSWORD_EXPIRED(1008, "密码已过期"),
    PASSWORD_ERROR_MAX_ATTEMPTS(1009, "密码错误次数过多，账户已锁定"),
    CAPTCHA_ERROR(1010, "验证码错误"),
    CAPTCHA_EXPIRED(1011, "验证码已过期"),
    REFRESH_TOKEN_EXPIRED(1012, "刷新Token已过期"),
    REFRESH_TOKEN_INVALID(1013, "刷新Token无效"),

    // ==================== 业务错误码 - 项目模块 (2000+) ====================
    PROJECT_NOT_FOUND(2001, "项目不存在"),
    PROJECT_CODE_EXISTS(2002, "项目编码已存在"),
    PROJECT_NAME_EXISTS(2003, "项目名称已存在"),
    PROJECT_DISABLED(2004, "项目已禁用"),
    PROJECT_MEMBER_NOT_FOUND(2005, "项目成员不存在"),
    PROJECT_MEMBER_EXISTS(2006, "项目成员已存在"),
    PROJECT_ENV_VAR_NOT_FOUND(2007, "环境变量不存在"),
    PROJECT_ENV_VAR_EXISTS(2008, "环境变量已存在"),
    GIT_CONNECTION_FAILED(2009, "Git仓库连接失败"),
    GIT_ACCESS_DENIED(2010, "Git仓库访问被拒绝"),
    GIT_BRANCH_NOT_FOUND(2011, "Git分支不存在"),
    GIT_COMMIT_NOT_FOUND(2012, "Git提交不存在"),

    // ==================== 业务错误码 - 构建模块 (3000+) ====================
    BUILD_NOT_FOUND(3001, "构建记录不存在"),
    BUILD_FAILED(3002, "构建失败"),
    BUILD_CANCELLED(3003, "构建已取消"),
    BUILD_RUNNING(3004, "构建正在进行中"),
    BUILD_TIMEOUT(3005, "构建超时"),
    PIPELINE_NOT_FOUND(3006, "流水线配置不存在"),
    PIPELINE_STAGE_FAILED(3007, "流水线阶段执行失败"),
    ARTIFACT_NOT_FOUND(3008, "构建产物不存在"),
    ARTIFACT_UPLOAD_FAILED(3009, "构建产物上传失败"),
    ARTIFACT_DOWNLOAD_FAILED(3010, "构建产物下载失败"),
    BUILD_BRANCH_NOT_ALLOWED(3011, "该分支不允许构建"),

    // ==================== 业务错误码 - 部署模块 (4000+) ====================
    DEPLOY_NOT_FOUND(4001, "部署记录不存在"),
    DEPLOY_FAILED(4002, "部署失败"),
    DEPLOY_CANCELLED(4003, "部署已取消"),
    DEPLOY_RUNNING(4004, "部署正在进行中"),
    DEPLOY_TIMEOUT(4005, "部署超时"),
    DEPLOY_PRECHECK_FAILED(4006, "部署前检查未通过"),
    DEPLOY_HEALTH_CHECK_FAILED(4007, "健康检查失败"),
    DEPLOY_SERVER_OFFLINE(4008, "目标服务器不在线"),
    DEPLOY_STRATEGY_NOT_SUPPORTED(4009, "不支持的部署策略"),
    VERSION_NOT_FOUND(4010, "版本不存在"),
    VERSION_DEPLOYED(4011, "该版本已部署"),
    ROLLBACK_NOT_ALLOWED(4012, "不允许回滚"),
    ROLLBACK_FAILED(4013, "回滚失败"),
    ROLLBACK_VERSION_NOT_FOUND(4014, "回滚版本不存在"),
    NO_DEPLOYABLE_VERSION(4015, "没有可部署的版本"),

    // ==================== 业务错误码 - 服务器模块 (5000+) ====================
    SERVER_NOT_FOUND(5001, "服务器不存在"),
    SERVER_OFFLINE(5002, "服务器不在线"),
    SERVER_CONNECTION_FAILED(5003, "服务器连接失败"),
    SERVER_SSH_FAILED(5004, "SSH连接失败"),
    SERVER_AUTH_FAILED(5005, "服务器认证失败"),
    SERVER_GROUP_NOT_FOUND(5006, "服务器分组不存在"),
    SERVER_GROUP_EXISTS(5007, "服务器分组已存在"),
    SERVER_IP_CONFLICT(5008, "服务器IP冲突"),
    SERVER_PORT_CONFLICT(5009, "服务器端口冲突"),
    SERVER_DISK_SPACE_INSUFFICIENT(5010, "服务器磁盘空间不足"),
    SERVER_IN_USE(5011, "服务器正在使用中，无法删除"),

    // ==================== 业务错误码 - 日志模块 (6000+) ====================
    LOG_NOT_FOUND(6001, "日志不存在"),
    LOG_QUERY_FAILED(6002, "日志查询失败"),
    LOG_EXPORT_FAILED(6003, "日志导出失败"),
    LOG_STREAM_FAILED(6004, "日志流连接失败"),

    // ==================== 业务错误码 - 通知模块 (7000+) ====================
    NOTIFY_CONFIG_NOT_FOUND(7001, "通知配置不存在"),
    NOTIFY_CHANNEL_NOT_SUPPORTED(7002, "不支持的通知渠道"),
    NOTIFY_SEND_FAILED(7003, "通知发送失败"),
    NOTIFY_TEMPLATE_NOT_FOUND(7004, "通知模板不存在"),
    NOTIFY_WEBHOOK_FAILED(7005, "Webhook调用失败"),

    // ==================== 业务错误码 - 系统模块 (8000+) ====================
    USER_ALREADY_EXISTS(8001, "用户已存在"),
    ROLE_NOT_FOUND(8002, "角色不存在"),
    ROLE_EXISTS(8003, "角色已存在"),
    PERMISSION_NOT_FOUND(8004, "权限不存在"),
    PERMISSION_DENIED(8005, "权限不足"),
    CONFIG_NOT_FOUND(8006, "配置不存在"),
    CONFIG_UPDATE_FAILED(8007, "配置更新失败"),
    SYSTEM_BUSY(8008, "系统繁忙，请稍后重试"),
    MAINTENANCE_MODE(8009, "系统正在维护中");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 消息
     */
    private final String message;

    /**
     * 构造函数
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取消息
     */
    public String getMessage() {
        return message;
    }
}
