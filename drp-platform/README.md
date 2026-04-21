# DRP Platform

DevOps Release Platform - 面向中小团队的轻量级 DevOps 发布平台

## 项目简介

DRP 是一个轻量级的 DevOps 发布管理平台，提供项目管理、环境变量管理、分支策略管理等功能，帮助团队简化发布流程。

## 技术栈

- **后端**: Java 17 + Spring Boot 3.2 + MyBatis-Plus + Spring Security
- **前端**: Vue 3 + TypeScript + Element Plus
- **数据库**: MySQL
- **缓存**: Redis

## 模块说明

```
drp-platform/
├── drp-common          # 公共模块，包含工具类、响应封装等
├── drp-user-api        # 用户服务 API 定义（Feign 接口）
├── drp-auth            # 认证服务，用户认证授权模块
├── drp-project         # 项目管理服务
├── drp-sql             # SQL 脚本模块
└── drp-boot            # 启动模块，聚合所有服务
```

### 各模块功能

| 模块 | 说明 |
|------|------|
| drp-common | 公共代码：结果封装、异常定义、常量等 |
| drp-user-api | 用户 API 接口定义，供其他模块调用 |
| drp-auth | 用户认证、授权、JWT 令牌管理 |
| drp-project | 项目管理：项目成员、环境变量、分支策略 |
| drp-sql | 数据库初始化脚本和升级 DDL |
| drp-boot | 启动入口，聚合所有模块 |

## 项目结构

```
drp-platform/
├── src/main/java/
│   └── com/drp/
│       ├── common/          # 公共模块
│       ├── auth/            # 认证服务
│       ├── project/         # 项目服务
│       └── boot/            # 启动类
└── src/main/resources/
    └── application.yml      # 配置文件
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 编译项目

```bash
cd drp-platform
mvn clean install -DskipTests
```

### 配置数据库

1. 创建数据库 `drp`
2. 执行 SQL 脚本初始化表结构

```sql
-- 创建数据库
CREATE DATABASE drp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 初始化表结构
source drp-sql/src/main/sql/init/drp_init.sql
```

### 修改配置

编辑 `drp-boot/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/drp?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

### 启动服务

```bash
cd drp-boot
mvn spring-boot:run
```

服务启动后访问：`http://localhost:8081`

## 主要功能

### 项目管理
- 创建和管理项目
- 关联 Git 仓库
- 配置构建信息

### 成员管理
- 添加项目成员
- 配置成员角色（所有者/开发者/观察者）
- 细粒度权限控制

### 环境变量
- 按环境（开发/测试/生产）管理变量
- 敏感信息加密存储

### 分支策略
- 定义分支命名规范
- 配置保护规则

## API 文档

| 服务 | 端点 | 说明 |
|------|------|------|
| 认证服务 | POST /api/auth/login | 用户登录 |
| | POST /api/auth/refresh | 刷新令牌 |
| 用户服务 | GET /api/users | 分页查询用户 |
| | GET /api/users/{id} | 获取用户详情 |
| 项目服务 | GET /api/projects | 分页查询项目 |
| | GET /api/projects/{id} | 获取项目详情 |
| | GET /api/projects/{id}/members | 获取项目成员 |

## 开发指南

### 添加新模块

1. 在父 POM 中添加模块引用
2. 创建模块目录结构
3. 在 `drp-boot` 的 `pom.xml` 中添加依赖

### 模块间调用

- **单体模式**：通过接口直接调用（如 `UserServiceInterface`）
- **微服务模式**：通过 Feign Client 远程调用

## 许可证

Private Project - All Rights Reserved