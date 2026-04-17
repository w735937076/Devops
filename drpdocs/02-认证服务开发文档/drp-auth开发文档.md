# DRP 平台认证服务 (drp-auth) 开发文档

## 1. 模块概述

### 1.1 模块简介

drp-auth 是 DRP (DevOps Release Platform) 平台的认证服务模块，负责用户的身份认证、授权管理、Token 管理等核心安全功能。

### 1.2 主要功能

| 功能 | 描述 | 优先级 |
|-----|------|--------|
| 用户登录 | 用户名密码认证，返回 JWT Token | P0 |
| 用户登出 | 注销 Token，清除会话 | P0 |
| Token 刷新 | 使用 Refresh Token 获取新的 Access Token | P0 |
| 获取用户信息 | 获取当前登录用户的详细信息 | P0 |
| Token 验证 | 验证 Token 的有效性 | P1 |

### 1.3 技术栈

| 组件 | 选型 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.2.5 |
| 安全框架 | Spring Security | 6.x |
| 认证协议 | JWT (JJWT) | 0.12.5 |
| 密码加密 | BCrypt | 内置 |
| ORM | MyBatis Plus | 3.5.6 |
| 缓存 | Redis | 7.x |
| 数据库 | MySQL | 8.0 |

---

## 2. 系统架构

### 2.1 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              客户端请求                                      │
│                         (Browser / Mobile / API Client)                      │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          JwtAuthenticationFilter                            │
│                         (JWT Token 解析与认证)                               │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │  1. 从请求头提取 Authorization: Bearer {token}                        │  │
│  │  2. 验证 Token 有效性                                                 │  │
│  │  3. 提取用户信息并设置 SecurityContext                               │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Security Filter Chain                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ CORS Filter  │→│ Session Filter│→│Auth Filter   │→│Resource Filter│  │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          │                       │                       │
          ▼                       ▼                       ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  AuthController  │  │  UserDetails     │  │    Redis        │
│                  │  │  ServiceImpl    │  │                  │
│  /api/auth/*     │  │                  │  │  Token 存储     │
│                  │  │  用户认证        │  │  会话管理       │
└────────┬─────────┘  └────────┬─────────┘  └──────────────────┘
         │                     │
         │                     ▼
         │           ┌──────────────────┐
         │           │   MySQL 数据库   │
         │           │                  │
         │           │  sys_user       │
         │           │  sys_role      │
         │           │  sys_permission │
         │           └──────────────────┘
         ▼
┌──────────────────┐
│  AuthService    │
│                  │
│  业务逻辑处理    │
└──────────────────┘
```

### 2.2 Token 流程图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Token 生命周期                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. [登录] ─────────────────────────────────────────────────────────────  │
│     │                                                                    │
│     │  POST /api/auth/login {username, password}                          │
│     │                                                                    │
│     ▼                                                                    │
│  2. [认证] ─────────────────────────────────────────────────────────────  │
│     │                                                                    │
│     │  Spring Security AuthenticationManager                               │
│     │  BCrypt 密码验证                                                    │
│     │                                                                    │
│     ▼                                                                    │
│  3. [生成 Token] ──────────────────────────────────────────────────────  │
│     │                                                                    │
│     │  JwtTokenProvider.generateAccessToken() ──→ Access Token (15分钟)  │
│     │  JwtTokenProvider.generateRefreshToken() ──→ Refresh Token (7天)   │
│     │                                                                    │
│     ▼                                                                    │
│  4. [存储 Token] ─────────────────────────────────────────────────────  │
│     │                                                                    │
│     │  Redis: auth:token:{username} ──→ Access Token                     │
│     │  Redis: auth:refresh:{username} ──→ Refresh Token                  │
│     │                                                                    │
│     ▼                                                                    │
│  5. [返回] ────────────────────────────────────────────────────────────  │
│     │                                                                    │
│     │  { accessToken, refreshToken, expiresIn, userInfo }                 │
│     │                                                                    │
│     ▼                                                                    │
│  6. [后续请求] ───────────────────────────────────────────────────────  │
│     │                                                                    │
│     │  Header: Authorization: Bearer {accessToken}                        │
│     │                                                                    │
│     ▼                                                                    │
│  7. [Token 过期] ─────────────────────────────────────────────────────  │
│     │                                                                    │
│     │  POST /api/auth/refresh { refreshToken }                           │
│     │  返回新的 Access Token + Refresh Token                              │
│     │                                                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. 项目结构

```
drp-auth/
├── pom.xml
├── src/main/java/com/drp/auth/
│   │
│   ├── AuthApplication.java              # 应用启动类
│   │
│   ├── controller/                      # 控制层
│   │   └── AuthController.java         # 认证控制器
│   │
│   ├── service/                         # 服务层
│   │   ├── AuthService.java            # 认证服务接口
│   │   └── impl/
│   │       └── AuthServiceImpl.java    # 认证服务实现
│   │
│   ├── entity/                         # 实体层
│   │   ├── SysUser.java               # 用户实体
│   │   ├── SysRole.java               # 角色实体
│   │   ├── SysPermission.java          # 权限实体
│   │   └── SysUserRole.java           # 用户角色关联实体
│   │
│   ├── repository/                     # 数据访问层
│   │   ├── SysUserRepository.java     # 用户 Mapper
│   │   ├── SysRoleRepository.java     # 角色 Mapper
│   │   ├── SysPermissionRepository.java # 权限 Mapper
│   │   └── SysUserRoleRepository.java  # 用户角色 Mapper
│   │
│   ├── dto/                           # 数据传输对象
│   │   ├── LoginRequest.java          # 登录请求
│   │   ├── LoginResponse.java         # 登录响应
│   │   ├── RefreshTokenRequest.java   # 刷新 Token 请求
│   │   └── UserDTO.java              # 用户信息 DTO
│   │
│   ├── security/                       # 安全组件
│   │   ├── SecurityConfig.java        # Spring Security 配置
│   │   ├── JwtTokenProvider.java      # JWT Token 提供者
│   │   ├── JwtAuthenticationFilter.java # JWT 认证过滤器
│   │   ├── JwtAuthenticationEntryPoint.java # 认证入口点
│   │   └── UserDetailsServiceImpl.java # 用户详情服务
│   │
│   └── exception/                      # 异常处理
│       ├── AuthException.java         # 认证异常
│       └── AuthExceptionHandler.java  # 认证异常处理器
│
└── src/main/resources/
    ├── application.yml                 # 应用配置
    └── sql/
        └── init-auth.sql             # 数据库初始化脚本
```

---

## 4. 数据库设计

### 4.1 ER 图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────────┐
│  sys_user   │       │  sys_role   │       │ sys_permission  │
├─────────────┤       ├─────────────┤       ├─────────────────┤
│ id (PK)     │←──┐   │ id (PK)     │←──┐   │ id (PK)         │
│ username    │   │   │ code        │   │   │ code           │
│ password    │   │   │ name        │   │   │ name           │
│ real_name   │   └──→│ user_id(FK) │   └──→│ role_id (FK)   │
│ email       │       └─────────────┘       └─────────────────┘
│ phone       │
│ status      │       ┌─────────────────────┐
│ ...         │       │   sys_user_role     │
└─────────────┘       ├─────────────────────┤
                      │ id (PK)             │
                      │ user_id (FK) ──────┘
                      │ role_id (FK) ──────┘
                      └─────────────────────┘
```

### 4.2 表结构

#### sys_user (用户表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | BCrypt 加密密码 |
| real_name | VARCHAR(100) | 真实姓名 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| avatar | VARCHAR(255) | 头像 URL |
| status | TINYINT | 状态: 0-禁用, 1-启用 |
| last_login_time | DATETIME | 最后登录时间 |
| last_login_ip | VARCHAR(50) | 最后登录 IP |
| create_time | DATETIME | 创建时间 |
| create_by | BIGINT | 创建人 |
| update_time | DATETIME | 更新时间 |
| update_by | BIGINT | 更新人 |
| deleted | TINYINT | 逻辑删除标记 |

#### sys_role (角色表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| code | VARCHAR(50) | 角色编码，唯一 |
| name | VARCHAR(100) | 角色名称 |
| description | VARCHAR(255) | 角色描述 |
| type | VARCHAR(20) | 类型: SYSTEM/CUSTOM |
| sort | INT | 排序号 |
| status | TINYINT | 状态: 0-禁用, 1-启用 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### sys_permission (权限表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| code | VARCHAR(100) | 权限编码，唯一 |
| name | VARCHAR(100) | 权限名称 |
| description | VARCHAR(255) | 权限描述 |
| parent_id | BIGINT | 父权限 ID |
| type | VARCHAR(20) | 类型: MENU/BUTTON/API |
| sort | INT | 排序号 |
| path | VARCHAR(200) | 路由路径 |
| component | VARCHAR(200) | 组件路径 |
| status | TINYINT | 状态: 0-禁用, 1-启用 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 4.3 初始数据

**角色**:
| code | name | description | 权限数量 |
|------|------|-------------|---------|
| ADMIN | 系统管理员 | 拥有系统所有权限 | 18 |
| DEVELOPER | 开发人员 | 开发人员角色 | 9 |
| VIEWER | 查看者 | 只读权限 | 0 |

**默认用户**:
| username | password | role | 密码说明 |
|----------|----------|------|---------|
| admin | admin123 | ADMIN | 初始管理员 |
| dev | admin123 | DEVELOPER | 初始开发者 |

---

## 5. API 接口规范

### 5.1 登录接口

**请求**
```http
POST /api/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "admin123"
}
```

**响应 (成功)**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 900,
        "userId": 1,
        "username": "admin",
        "realName": "管理员",
        "email": "admin@drp.com",
        "phone": null,
        "avatar": null,
        "roles": ["ADMIN"],
        "permissions": ["PROJECT_VIEW", "PROJECT_CREATE", ...],
        "loginTime": "2026-04-18 00:30:00"
    },
    "timestamp": 1713388200000,
    "traceId": "abc123def456"
}
```

**响应 (失败)**
```json
{
    "code": 1002,
    "message": "用户名或密码错误",
    "data": null,
    "timestamp": 1713388200000,
    "traceId": "abc123def456"
}
```

### 5.2 登出接口

**请求**
```http
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

**响应**
```json
{
    "code": 200,
    "message": "登出成功",
    "data": null,
    "timestamp": 1713388300000,
    "traceId": "def456ghi789"
}
```

### 5.3 刷新 Token 接口

**请求**
```http
POST /api/auth/refresh
Refresh-Token: {refreshToken}
```

**响应**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 900
    },
    "timestamp": 1713388400000,
    "traceId": "ghi789jkl012"
}
```

### 5.4 获取用户信息接口

**请求**
```http
GET /api/auth/userinfo
Authorization: Bearer {accessToken}
```

**响应**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "username": "admin",
        "realName": "管理员",
        "email": "admin@drp.com",
        "phone": null,
        "avatar": null,
        "status": 1,
        "statusDesc": "启用",
        "roles": [
            {"id": 1, "code": "ADMIN", "name": "系统管理员"}
        ],
        "permissions": ["PROJECT_VIEW", "PROJECT_CREATE", ...],
        "lastLoginTime": "2026-04-18 00:30:00",
        "lastLoginIp": "127.0.0.1",
        "createTime": "2026-04-18 00:00:00",
        "createBy": null
    },
    "timestamp": 1713388500000,
    "traceId": "jkl012mno345"
}
```

### 5.5 验证 Token 接口

**请求**
```http
GET /api/auth/validate
Authorization: Bearer {accessToken}
```

**响应**
```json
{
    "code": 200,
    "message": "操作成功",
    "data": true,
    "timestamp": 1713388600000,
    "traceId": "mno345pqr678"
}
```

---

## 6. 核心组件详解

### 6.1 JwtTokenProvider

**文件位置**: `com.drp.auth.security.JwtTokenProvider`

**职责**: JWT Token 的生成、验证、解析

**核心方法**:

```java
// 生成 Access Token
public String generateAccessToken(UserDetails userDetails)

// 生成 Refresh Token
public String generateRefreshToken(UserDetails userDetails)

// 验证 Token 有效性
public boolean validateToken(String token)

// 提取用户名
public String extractUsername(String token)

// 判断 Token 类型
public boolean isAccessToken(String token)
public boolean isRefreshToken(String token)

// 获取 Token 剩余有效时间
public long getRemainingTime(String token)
```

**Token 结构**:

```json
// Header
{
    "alg": "HS256",
    "typ": "JWT"
}

// Payload (Access Token)
{
    "sub": "admin",
    "username": "admin",
    "roles": ["ROLE_ADMIN"],
    "type": "access",
    "iat": 1713388200,
    "exp": 1713389100
}

// Payload (Refresh Token)
{
    "sub": "admin",
    "type": "refresh",
    "iat": 1713388200,
    "exp": 1713395400
}
```

### 6.2 JwtAuthenticationFilter

**文件位置**: `com.drp.auth.security.JwtAuthenticationFilter`

**职责**: 拦截请求，解析 JWT Token，设置认证信息

**过滤流程**:

```
1. 请求进入 doFilterInternal()
       │
       ▼
2. extractJwtFromRequest(request)
   从 Header: Authorization: Bearer {token} 提取 Token
       │
       ▼
3. validateToken(token) 验证 Token
   ├── 无 Token → 放行，继续 Filter Chain
   ├── Token 无效 → 记录警告，放行
   └── Token 有效 → 继续
       │
       ▼
4. extractUsername(token) 提取用户名
       │
       ▼
5. loadUserByUsername(username) 加载用户
       │
       ▼
6. 创建 UsernamePasswordAuthenticationToken
       │
       ▼
7. SecurityContextHolder.getContext().setAuthentication()
       │
       ▼
8. filterChain.doFilter() 继续执行
```

### 6.3 SecurityConfig

**文件位置**: `com.drp.auth.security.SecurityConfig`

**配置内容**:

```java
// 1. 禁用 CSRF
.csrf(AbstractHttpConfigurer::disable)

// 2. 配置 CORS
.cors(cors -> cors.configurationSource(corsConfigurationSource()))

// 3. 无状态会话
.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

// 4. 请求授权
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/login").permitAll()
    .requestMatchers("/api/auth/refresh").permitAll()
    .requestMatchers("/actuator/**").permitAll()
    .anyRequest().authenticated())

// 5. 添加 JWT 过滤器
.addFilterBefore(jwtAuthenticationFilter,
    UsernamePasswordAuthenticationFilter.class)

// 6. 认证失败处理
.exceptionHandling(exceptions -> exceptions
    .authenticationEntryPoint(authenticationEntryPoint))
```

### 6.4 AuthServiceImpl

**文件位置**: `com.drp.auth.service.impl.AuthServiceImpl`

**核心方法**:

```java
// 登录
public LoginResponse login(LoginRequest request, String ip)
// 1. AuthenticationManager 认证
// 2. 生成 Access/Refresh Token
// 3. 存储到 Redis
// 4. 更新登录信息
// 5. 返回登录响应

// 登出
public void logout(String username)
// 1. 删除 Redis 中的 Token
// 2. 清除 SecurityContext

// 刷新 Token
public LoginResponse refresh(RefreshTokenRequest request)
// 1. 验证 Refresh Token
// 2. 检查 Redis 中 Token 是否匹配
// 3. 生成新 Token
// 4. 更新 Redis
// 5. 返回新响应

// 获取当前用户
public UserDTO getCurrentUser(String username)
// 1. 查询用户信息
// 2. 查询角色权限
// 3. 组装 DTO 返回
```

---

## 7. Redis 存储设计

### 7.1 Key 命名规范

| Key Pattern | 说明 | 过期时间 |
|-------------|------|---------|
| `auth:token:{username}` | Access Token | 15分钟 |
| `auth:refresh:{username}` | Refresh Token | 7天 |

### 7.2 Token 存储结构

```
# Access Token 存储
Key: auth:token:admin
Value: eyJhbGciOiJIUzI1NiJ9...
TTL: 900 秒 (15分钟)

# Refresh Token 存储
Key: auth:refresh:admin
Value: eyJhbGciOiJIUzI1NiJ9...
TTL: 604800 秒 (7天)
```

### 7.3 登出时清理

```java
// 登出时删除所有相关 Token
redisTemplate.delete("auth:token:" + username);
redisTemplate.delete("auth:refresh:" + username);
```

---

## 8. 安全设计

### 8.1 密码安全

- **加密算法**: BCrypt (单向哈希)
- **盐值**: BCrypt 自动生成随机盐值
- **强度因子**: 10 (2^10 次迭代)
- **密码验证**: BCrypt.matches(rawPassword, encodedPassword)

### 8.2 JWT 安全

- **签名算法**: HMAC-SHA256 (HS256)
- **密钥长度**: 256 位
- **密钥存储**: Base64 编码配置
- **Token 有效期**: Access Token 15分钟 / Refresh Token 7天

### 8.3 安全响应

```java
// 认证失败 - 不区分用户不存在和密码错误
// 防止用户名枚举攻击
if (e instanceof BadCredentialsException) {
    message = "用户名或密码错误";  // 统一错误信息
}
```

### 8.4 公开接口 (免认证)

| 接口 | 路径 |
|------|------|
| 登录 | /api/auth/login |
| 刷新 Token | /api/auth/refresh |
| 验证码 | /api/auth/captcha |
| 健康检查 | /actuator/** |

---

## 9. 配置说明

### 9.1 application.yml

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/drp
    username: root
    password: 1q2w3e4r

  data:
    redis:
      host: localhost
      port: 6379
      password: 1Q2w3e4r!

jwt:
  secret: <Base64编码的256位密钥>
  expiration-ms: 900000        # 15分钟
  refresh-expiration-ms: 604800000  # 7天
```

### 9.2 环境变量覆盖

```bash
# 生产环境推荐使用环境变量
export JWT_SECRET=<your-secret-key>
export SPRING_DATASOURCE_PASSWORD=<your-db-password>
export SPRING_DATA_REDIS_PASSWORD=<your-redis-password>
```

---

## 10. 错误码

### 10.1 认证模块错误码

| 错误码 | 说明 |
|-------|------|
| 1001 | 用户不存在 |
| 1002 | 用户名或密码错误 |
| 1003 | Token 已过期 |
| 1004 | Token 无效 |
| 1005 | Token 格式错误 |
| 1006 | 用户已被禁用 |
| 1007 | 用户已被锁定 |
| 1008 | 密码已过期 |
| 1009 | 密码错误次数过多 |
| 1010 | 验证码错误 |
| 1011 | 验证码已过期 |
| 1012 | Refresh Token 已过期 |
| 1013 | Refresh Token 无效 |

---

## 11. 使用指南

### 11.1 快速开始

**1. 初始化数据库**
```bash
mysql -u root -p < src/main/resources/sql/init-auth.sql
```

**2. 配置 Redis**
确保 Redis 服务运行在 localhost:6379

**3. 启动服务**
```bash
mvn spring-boot:run
```

**4. 测试登录**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 11.2 客户端集成

```javascript
// 登录
const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({username, password})
});
const {data} = await response.json();
localStorage.setItem('accessToken', data.accessToken);
localStorage.setItem('refreshToken', data.refreshToken);

// 后续请求
const token = localStorage.getItem('accessToken');
fetch('/api/protected', {
    headers: {'Authorization': `Bearer ${token}`}
});

// Token 过期时刷新
await fetch('/api/auth/refresh', {
    method: 'POST',
    headers: {'Refresh-Token': refreshToken}
});
```

---

## 12. 测试账号

| 账号 | 密码 | 角色 | 权限数量 |
|------|------|------|---------|
| admin | admin123 | ADMIN | 18 |
| dev | admin123 | DEVELOPER | 9 |

---

*文档更新时间: 2026-04-18*
