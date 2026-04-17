package com.drp.common.constant;

/**
 * 系统常量
 *
 * @author Nick
 */
public class SystemConstant {

    private SystemConstant() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    // ==================== 通用常量 ====================

    /**
     * UTF-8 编码
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 编码
     */
    public static final String GBK = "GBK";

    /**
     * JSON Content-Type
     */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * XML Content-Type
     */
    public static final String CONTENT_TYPE_XML = "application/xml";

    /**
     * 空字符串
     */
    public static final String EMPTY = "";

    /**
     * 逗号
     */
    public static final String COMMA = ",";

    /**
     * 点号
     */
    public static final String DOT = ".";

    /**
     * 下划线
     */
    public static final String UNDERSCORE = "_";

    /**
     * 横线
     */
    public static final String HYPHEN = "-";

    /**
     * 斜杠
     */
    public static final String SLASH = "/";

    /**
     * 反斜杠
     */
    public static final String BACK_SLASH = "\\";

    // ==================== 分页常量 ====================

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    // ==================== 缓存常量 ====================

    /**
     * 缓存 key 前缀 - 用户
     */
    public static final String CACHE_KEY_USER = "user:";

    /**
     * 缓存 key 前缀 - 角色
     */
    public static final String CACHE_KEY_ROLE = "role:";

    /**
     * 缓存 key 前缀 - 权限
     */
    public static final String CACHE_KEY_PERMISSION = "permission:";

    /**
     * 缓存 key 前缀 - 项目
     */
    public static final String CACHE_KEY_PROJECT = "project:";

    /**
     * 缓存 key 前缀 - Token
     */
    public static final String CACHE_KEY_TOKEN = "token:";

    /**
     * 缓存 key 前缀 - 验证码
     */
    public static final String CACHE_KEY_CAPTCHA = "captcha:";

    /**
     * 缓存 key 前缀 - 限流
     */
    public static final String CACHE_KEY_RATE_LIMIT = "rate_limit:";

    /**
     * 缓存 key 前缀 - 防重复提交
     */
    public static final String CACHE_KEY_REPEAT_SUBMIT = "repeat_submit:";

    // ==================== Redis Key 过期时间 ====================

    /**
     * Token 过期时间（秒）- 2小时
     */
    public static final long EXPIRE_TOKEN = 7200L;

    /**
     * RefreshToken 过期时间（秒）- 7天
     */
    public static final long EXPIRE_REFRESH_TOKEN = 604800L;

    /**
     * 验证码过期时间（秒）- 5分钟
     */
    public static final long EXPIRE_CAPTCHA = 300L;

    /**
     * 短信验证码过期时间（秒）- 1分钟
     */
    public static final long EXPIRE_SMS_CODE = 60L;

    /**
     * 限流过期时间（秒）- 1分钟
     */
    public static final long EXPIRE_RATE_LIMIT = 60L;

    // ==================== 请求头常量 ====================

    /**
     * Authorization 请求头
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Bearer Token 前缀
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * X-Trace-Id 请求头
     */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * X-Request-Id 请求头
     */
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    /**
     * X-Tenant-Id 请求头
     */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    /**
     * User-Agent 请求头
     */
    public static final String HEADER_USER_AGENT = "User-Agent";

    /**
     * Real-IP 请求头（多级代理场景）
     */
    public static final String HEADER_REAL_IP = "X-Real-IP";

    // ==================== JWT 常量 ====================

    /**
     * JWT 密钥配置key
     */
    public static final String JWT_SECRET_KEY = "jwt.secret";

    /**
     * JWT 过期时间配置key
     */
    public static final String JWT_EXPIRE_KEY = "jwt.expire";

    /**
     * JWT 签发者
     */
    public static final String JWT_ISSUER = "drp-platform";

    // ==================== 加密常量 ====================

    /**
     * AES 密钥配置key
     */
    public static final String AES_SECRET_KEY = "encrypt.key";

    // ==================== 文件路径常量 ====================

    /**
     * 临时文件目录
     */
    public static final String TEMP_PATH = "/tmp/drp";

    /**
     * 日志文件目录
     */
    public static final String LOG_PATH = "/var/log/drp";

    /**
     * 构建产物目录
     */
    public static final String ARTIFACT_PATH = "/opt/drp/artifacts";

    /**
     * 备份目录
     */
    public static final String BACKUP_PATH = "/opt/drp/backup";

    // ==================== 时间常量 ====================

    /**
     * 秒转毫秒
     */
    public static final long SECOND_MILLIS = 1000L;

    /**
     * 分钟转毫秒
     */
    public static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;

    /**
     * 小时转毫秒
     */
    public static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;

    /**
     * 天转毫秒
     */
    public static final long DAY_MILLIS = 24 * HOUR_MILLIS;
}
