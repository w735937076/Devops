# DRP 平台公共模块 (drp-common) 开发文档

## 1. 模块概述

### 1.1 模块简介
drp-common 是 DRP (DevOps Release Platform) 平台的公共模块，为所有业务服务提供基础能力支撑。

### 1.2 主要功能
- 统一响应封装 (Result)
- 统一异常处理 (GlobalExceptionHandler)
- 分页请求/响应封装 (PageRequest/PageResponse)
- 实体基类 (BaseEntity)
- 工具类 (AesEncryptUtil, SnowflakeIdUtil, DateUtil, BeanUtil, SpringUtil)
- 框架配置 (Redis, MyBatis Plus, Jackson, Web)
- 业务错误码 (ResultCode, BusinessError)
- 系统常量 (SystemConstant)
- 通用枚举 (StatusEnum, DeletedEnum)
- 注解 (NoRepeatSubmit, RequestRateLimit, OperationLog)

### 1.3 技术栈
| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 基础框架 |
| MyBatis Plus | 3.5.x | ORM框架 |
| Redis | - | 缓存/Session |
| Jackson | 2.16.x | JSON处理 |
| Hutool | 5.8.x | 工具集 |
| JJWT | 0.12.x | JWT处理 |
| Fastjson2 | 2.0.x | 备用JSON |

---

## 2. 项目结构

```
drp-common/
├── pom.xml
└── src/main/java/com/drp/common/
    ├── annotation/          # 注解
    │   ├── NoRepeatSubmit.java       # 防重复提交
    │   ├── RequestRateLimit.java     # 接口限流
    │   └── OperationLog.java         # 操作日志
    ├── config/             # 配置类
    │   ├── RedisConfig.java           # Redis配置
    │   ├── JacksonConfig.java         # Jackson配置
    │   ├── MybatisPlusConfig.java     # MyBatis Plus配置
    │   └── WebConfig.java             # Web配置(CORS/追踪ID)
    ├── constant/           # 常量
    │   ├── ResultCode.java           # 响应码枚举
    │   ├── BusinessError.java        # 业务错误码
    │   └── SystemConstant.java       # 系统常量
    ├── dto/                # 数据传输对象
    │   ├── PageRequest.java          # 分页请求
    │   └── PageResponse.java         # 分页响应
    ├── entity/             # 实体基类
    │   └── BaseEntity.java           # 实体基类
    ├── enums/              # 枚举
    │   ├── StatusEnum.java           # 状态枚举
    │   └── DeletedEnum.java         # 删除状态枚举
    ├── exception/          # 异常
    │   ├── BusinessException.java    # 业务异常
    │   └── GlobalExceptionHandler.java # 全局异常处理器
    ├── result/             # 响应封装
    │   ├── Result.java              # 统一响应封装
    │   └── TraceContext.java        # 追踪上下文
    └── util/               # 工具类
        ├── AesEncryptUtil.java       # AES加密工具
        ├── SnowflakeIdUtil.java      # 雪花ID生成器
        ├── DateUtil.java             # 日期工具
        ├── BeanUtil.java             # Bean工具
        └── SpringUtil.java           # Spring工具
```

---

## 3. 核心组件详解

### 3.1 统一响应封装 (Result)

**文件位置**: `com.drp.common.result.Result`

**响应结构**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1713432000000,
  "traceId": "abc123def456",
  "requestId": null
}
```

**使用示例**:
```java
// 成功响应
return Result.success();                          // 无数据
return Result.success(data);                      // 带数据
return Result.success("自定义消息", data);        // 自定义消息

// 错误响应
return Result.error("错误消息");                  // 默认500
return Result.error(400, "自定义错误码");         // 自定义错误码
return Result.error(ResultCode.NOT_FOUND);        // 使用ResultCode
return Result.error(ResultCode.NOT_FOUND, "自定义"); // 使用ResultCode+自定义消息

// 分页响应
return Result.pageSuccess(pageResponse);
```

### 3.2 响应码 (ResultCode)

**文件位置**: `com.drp.common.constant.ResultCode`

**错误码规则**:
| 范围 | 类别 |
|------|------|
| 2xx | 成功 |
| 4xx | 客户端错误 |
| 5xx | 服务端错误 |
| 1000+ | 业务错误码 |

**业务错误码分配**:
| 模块 | 错误码范围 |
|------|------------|
| 认证模块 | 1001-1013 |
| 项目模块 | 2001-2060 |
| 构建模块 | 3001-3015 |
| 部署模块 | 4001-4015 |
| 服务器模块 | 5001-5015 |
| 日志模块 | 6001-6005 |
| 通知模块 | 7001-7005 |
| 系统模块 | 8001-8010 |

### 3.3 业务异常 (BusinessException)

**文件位置**: `com.drp.common.exception.BusinessException`

**使用示例**:
```java
// 抛出业务异常
throw new BusinessException(ResultCode.USER_NOT_FOUND);
throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户张三不存在");
throw new BusinessException(BusinessError.DATA_NOT_FOUND);
throw new BusinessException(1001, "自定义错误码和消息");
```

### 3.4 全局异常处理器 (GlobalExceptionHandler)

**文件位置**: `com.drp.common.exception.GlobalExceptionHandler`

**处理的异常类型**:
- `BusinessException` - 业务异常
- `MethodArgumentNotValidException` - 参数校验失败
- `ConstraintViolationException` - 约束校验失败
- `BindException` - 参数绑定失败
- `MissingServletRequestParameterException` - 缺少请求参数
- `MethodArgumentTypeMismatchException` - 参数类型不匹配
- `HttpMessageNotReadableException` - 请求体解析失败
- `HttpRequestMethodNotSupportedException` - 请求方法不支持
- `NoHandlerFoundException` - 404异常
- `Exception` - 其他未捕获异常

### 3.5 分页请求/响应

**PageRequest** - 分页请求封装
```java
// 基本分页
PageRequest pageRequest = new PageRequest();
pageRequest.setPage(1);
pageRequest.setSize(10);

// 排序分页
pageRequest.setSortBy("createTime");
pageRequest.setSortOrder("desc");

// 时间范围
pageRequest.setStartTime(LocalDateTime.now().minusDays(7));
pageRequest.setEndTime(LocalDateTime.now());

// 校验并修复参数
pageRequest.validate();

// 计算偏移量
int offset = pageRequest.getOffset();  // (page-1) * size
```

**PageResponse** - 分页响应封装
```java
// 构建分页响应
PageResponse<User> response = PageResponse.of(records, totalCount, page, size);

// 构建分页响应（带耗时）
PageResponse<User> response = PageResponse.of(records, totalCount, page, size, costTime);

// 创建空分页响应
PageResponse<User> empty = PageResponse.empty(page, size);

// 判断
if (response.isNotEmpty()) { ... }
if (response.isFirst()) { ... }
```

### 3.6 实体基类 (BaseEntity)

**文件位置**: `com.drp.common.entity.BaseEntity`

**字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID（自增） |
| createTime | LocalDateTime | 创建时间 |
| createBy | Long | 创建人ID |
| createName | String | 创建人名称（冗余） |
| updateTime | LocalDateTime | 更新时间 |
| updateBy | Long | 更新人ID |
| updateName | String | 更新人名称（冗余） |
| status | Integer | 状态（0禁用/1启用） |
| remark | String | 备注 |
| tenantId | Long | 租户ID |
| deleted | Boolean | 逻辑删除标记 |

**使用示例**:
```java
@TableName("sys_user")
public class User extends BaseEntity {
    private String username;
    private String password;
    // ... getters/setters
}
```

---

## 4. 工具类详解

### 4.1 AES加密工具 (AesEncryptUtil)

**文件位置**: `com.drp.common.util.AesEncryptUtil`

**特性**:
- 算法：AES/GCM/NoPadding（256位）
- 自动生成随机IV
- IV与密文合并存储

**使用示例**:
```java
// 使用默认密钥加密
String encrypted = AesEncryptUtil.encrypt("敏感数据");

// 使用默认密钥解密
String decrypted = AesEncryptUtil.decrypt(encrypted);

// 使用自定义密钥
String customKey = AesEncryptUtil.generateKey();  // 生成长度为32字节的Base64编码密钥
String encrypted2 = AesEncryptUtil.encrypt("数据", customKey);
String decrypted2 = AesEncryptUtil.decrypt(encrypted2, customKey);

// 验证密钥
boolean isValid = AesEncryptUtil.isValidKey(customKey);
```

### 4.2 雪花ID生成器 (SnowflakeIdUtil)

**文件位置**: `com.drp.common.util.SnowflakeIdUtil`

**特性**:
- Twitter Snowflake 算法
- 41位时间戳 + 10位机器ID + 12位序列号
- 支持时钟回拨检测（5ms内等待恢复）
- 机器ID范围：0-1023

**ID结构**:
```
| timestamp (41 bits) | machine (10 bits) | sequence (12 bits) |
```

**使用示例**:
```java
// 生成全局唯一ID（机器ID=0）
long id = SnowflakeIdUtil.generateId();
String idStr = SnowflakeIdUtil.generateIdStr();

// 创建指定机器ID的实例
SnowflakeIdUtil util = new SnowflakeIdUtil(5);  // 机器ID=5
long customId = util.nextId();

// 解析ID
long timestamp = SnowflakeIdUtil.getTimestamp(id);   // 获取时间戳
long machineId = SnowflakeIdUtil.getMachineId(id);   // 获取机器ID
long sequence = SnowflakeIdUtil.getSequence(id);     // 获取序列号
```

### 4.3 日期工具 (DateUtil)

**文件位置**: `com.drp.common.util.DateUtil`

**常量**:
```java
PATTERN_DATE = "yyyy-MM-dd"
PATTERN_TIME = "HH:mm:ss"
PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss"
PATTERN_COMPACT = "yyyyMMdd"
PATTERN_COMPACT_DATETIME = "yyyyMMddHHmmss"
```

**使用示例**:
```java
// 格式化
String dateStr = DateUtil.format(LocalDateTime.now());
String customStr = DateUtil.format(dateTime, "yyyy/MM/dd HH:mm");

// 解析
LocalDateTime dateTime = DateUtil.parseDateTime("2024-01-01 12:00:00");
LocalDate date = DateUtil.parseDate("2024-01-01");

// 转换
Date date = DateUtil.toDate(localDateTime);
LocalDateTime ldt = DateUtil.toLocalDateTime(date);

// 日期计算
long days = DateUtil.daysBetween(startDate, endDate);
long hours = DateUtil.hoursBetween(start, end);
long minutes = DateUtil.minutesBetween(start, end);

// 判断
boolean today = DateUtil.isToday(date);
boolean past = DateUtil.isPast(dateTime);
boolean future = DateUtil.isFuture(dateTime);
```

### 4.4 Bean工具 (BeanUtil)

**文件位置**: `com.drp.common.util.BeanUtil`

**使用示例**:
```java
// 复制属性
UserDTO dto = new UserDTO();
BeanUtil.copyProperties(user, dto);

// 复制并返回新对象
UserDTO dto = BeanUtil.copyProperties(user, UserDTO.class);

// 复制时排除空属性
BeanUtil.copyPropertiesIgnoreNull(user, dto);

// 获取空属性名
String[] nullProps = BeanUtil.getNullPropertyNames(user);

// JSON转换
String json = BeanUtil.toJson(user);
User user2 = BeanUtil.fromJson(json, User.class);
```

---

## 5. 配置类详解

### 5.1 Redis配置 (RedisConfig)

**文件位置**: `com.drp.common.config.RedisConfig`

**功能**:
- `RedisTemplate<String, Object>` - 对象类型Redis操作模板
- `StringRedisTemplate` - 字符串类型Redis操作模板
- `CacheManager` - 缓存管理器（支持多级缓存配置）

**缓存过期时间**:
| 缓存类型 | 过期时间 |
|----------|----------|
| 默认 | 1小时 |
| short | 5分钟 |
| long | 1天 |

### 5.2 Jackson配置 (JacksonConfig)

**文件位置**: `com.drp.common.config.JacksonConfig`

**功能**:
- Java 8 时间类型序列化/反序列化
- LocalDateTime: `yyyy-MM-dd HH:mm:ss`
- LocalDate: `yyyy-MM-dd`
- LocalTime: `HH:mm:ss`
- 禁用未知属性失败
- 禁用日期写为时间戳

### 5.3 MyBatis Plus配置 (MybatisPlusConfig)

**文件位置**: `com.drp.common.config.MybatisPlusConfig`

**功能**:
- 分页插件（MySQL）
- 自动填充处理器

**自动填充字段**:
| 操作 | 填充字段 |
|------|----------|
| INSERT | createTime, updateTime, status, deleted |
| UPDATE | updateTime |

### 5.4 Web配置 (WebConfig)

**文件位置**: `com.drp.common.config.WebConfig`

**功能**:
- 追踪ID过滤器（生成/传递 X-Trace-Id）
- CORS跨域配置（允许所有来源）

---

## 6. 注解详解

### 6.1 防重复提交 (NoRepeatSubmit)

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoRepeatSubmit {
    String prefix() default "";           // 锁key前缀
    long expireMillis() default 5000;    // 锁过期时间（毫秒）
    String message() default "请勿重复提交";
}
```

### 6.2 接口限流 (RequestRateLimit)

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestRateLimit {
    String key() default "";              // 限流key
    int timeWindow() default 60;         // 时间窗口（秒）
    int maxRequests() default 100;       // 最大请求次数
    String message() default "请求过于频繁，请稍后再试";
}
```

---

## 7. 常量与枚举

### 7.1 系统常量 (SystemConstant)

**缓存Key前缀**:
```java
CACHE_KEY_USER = "user:"
CACHE_KEY_ROLE = "role:"
CACHE_KEY_PERMISSION = "permission:"
CACHE_KEY_PROJECT = "project:"
CACHE_KEY_TOKEN = "token:"
CACHE_KEY_CAPTCHA = "captcha:"
CACHE_KEY_RATE_LIMIT = "rate_limit:"
CACHE_KEY_REPEAT_SUBMIT = "repeat_submit:"
```

**过期时间**:
```java
EXPIRE_TOKEN = 7200秒 (2小时)
EXPIRE_REFRESH_TOKEN = 604800秒 (7天)
EXPIRE_CAPTCHA = 300秒 (5分钟)
EXPIRE_RATE_LIMIT = 60秒 (1分钟)
```

**请求头**:
```java
HEADER_AUTHORIZATION = "Authorization"
HEADER_TRACE_ID = "X-Trace-Id"
HEADER_REQUEST_ID = "X-Request-Id"
HEADER_TENANT_ID = "X-Tenant-Id"
HEADER_REAL_IP = "X-Real-IP"
```

### 7.2 状态枚举 (StatusEnum)

```java
DISABLED(0, "禁用")
ENABLED(1, "启用")
DELETED(2, "已删除")
```

---

## 8. 依赖配置

### 8.1 Maven依赖

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- MyBatis Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.fastjson2</groupId>
        <artifactId>fastjson2</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### 8.2 编译配置

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>17</source>
                <target>17</target>
                <encoding>UTF-8</encoding>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 9. 使用指南

### 9.1 快速开始

**1. 引入依赖**
```xml
<dependency>
    <groupId>com.drp</groupId>
    <artifactId>drp-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

**2. 继承实体基类**
```java
@TableName("sys_user")
public class User extends BaseEntity {
    private String username;
    private String password;
    // getters/setters
}
```

**3. 使用统一响应**
```java
@RestController
public class UserController {
    @GetMapping("/user/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }
}
```

**4. 抛出业务异常**
```java
if (user == null) {
    throw new BusinessException(ResultCode.USER_NOT_FOUND);
}
```

### 9.2 最佳实践

1. **统一响应格式**: 所有Controller方法返回 `Result<T>`
2. **业务异常**: 使用 `BusinessException` 替代普通异常
3. **分页查询**: 使用 `PageRequest` 和 `PageResponse`
4. **实体类**: 继承 `BaseEntity` 以获得审计字段
5. **敏感数据**: 使用 `AesEncryptUtil` 加密存储
6. **分布式ID**: 使用 `SnowflakeIdUtil` 生成唯一ID

---

## 10. 版本信息

| 项目 | 版本 |
|------|------|
| DRP Platform | 1.0.0 |
| drp-common | 1.0.0 |
| Java | 17+ |
| Spring Boot | 3.2.5 |

---

*文档更新时间: 2026-04-18*
