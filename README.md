# DRP - DevOps Release Platform

> *在代码的海洋里，每一次发布都是一场精心编排的交响乐。*
>
> *DRP，赋能团队，让交付如行云流水。*

## 缘起

互联网江湖风云变幻，团队协作之道，在于高效的**持续交付**。DRP（DevOps Release Platform）应运而生——一个面向中小团队的轻量级 DevOps 发布平台，以工匠之心，打磨每一个交付细节。

---

## 技术哲学

**"高内聚、低耦合"** —— 这是软件工程的古老智慧，也是 DRP 的设计灵魂。

我们摒弃了单体架构的臃肿，拥抱模块化的优雅。后端采用 **Java 17** + **Spring Boot 3** 构建微内核，前端以 **Vue 3** + **Composition API** 诠释响应式之美。模块之间通过 **Feign Client** 解耦，却又通过 `drp-user-api` 共享契约——正如星际舰队，既各自独立，又能协同作战。

---

## 技术架构

```
┌─────────────────────────────────────────────────────────────────┐
│                           DRP                                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐        │
│  │   drp-ui    │    │  drp-boot    │    │    docs      │        │
│  │  Vue 3 + TS │    │  聚合入口     │    │   文档中心    │        │
│  └──────────────┘    └──────────────┘    └──────────────┘        │
│         │                   │                                      │
│         │              ┌─────┴─────┐                                │
│         │              │           │                                │
│         │         ┌────▼───┐  ┌───▼────┐  ┌───────────┐         │
│         │         │ drp-   │  │ drp-   │  │ drp-     │         │
│         │         │ auth   │  │project  │  │ server   │         │
│         │         │ 认证   │  │ 项目   │  │ 服务器   │         │
│         │         │ 服务   │  │ 服务   │  │ 管理     │         │
│         │         └───┬───┘  └───┬───┘  └───┬─────┘         │
│         │             │           │            │                  │
│         │        ┌─────▼───────────▼──────────▼─────┐            │
│         │        │            drp-common             │            │
│         │        │             公共基石               │            │
│         │        └──────────────────────────────────┘            │
│         │                                                               │
│         └──────────────────────┐                                   │
│                                ▼                                    │
│                     ┌─────────────────┐                             │
│                     │   MySQL + Redis │                             │
│                     │   数据存储层     │                             │
│                     └─────────────────┘                             │
└─────────────────────────────────────────────────────────────────┘
```

### 后端技术栈

| 层级 | 技术选型 | 技术注解 |
|------|---------|---------|
| 核心框架 | Spring Boot 3.2.5 | 基于 Java 17 的新一代响应式编程基石 |
| 安全框架 | Spring Security | 统一的认证授权中枢，支持 JWT 无状态会话 |
| ORM | MyBatis-Plus 3.5.6 | MyBatis 的瑞士军刀，CRUD 从此优雅 |
| 服务通信 | Spring Cloud OpenFeign | 声明式 HTTP 客户端，模块间调用如丝般顺滑 |
| 工具库 | Hutool 5.8 + FastJSON2 | 国产精品，简化 80% 的样板代码 |
| 会话管理 | Redis + Spring Session | 分布式会话的工业级解决方案 |

### 前端技术栈

| 层级 | 技术选型 | 技术注解 |
|------|---------|---------|
| 核心框架 | Vue 3.4 + Composition API | 告别 Options API 的束缚，拥抱函数的自由 |
| 状态管理 | Pinia 2.1 | Vue 3 官方推荐，比 Vuex 更轻盈 |
| 路由管理 | Vue Router 4.3 | SPA 导航的艺术家 |
| HTTP 客户端 | Axios 1.6 | 拦截器 + 统一错误处理 = 完美的网络层 |
| UI 组件库 | Element Plus 2.5 | 来自饿了么的诚意之作，开箱即用 |
| 构建工具 | Vite 5.2 | 革新性极速开发体验，HRM 毫秒级响应 |
| 语言 | TypeScript 5.4 | JavaScript 的铠甲，让类型安全成为习惯 |

### 前端 UI 设计规范

| 类别 | 规范 | 说明 |
|------|------|------|
| 主题色 | `#667eea` → `#764ba2` | 紫蓝渐变，贯穿全局 |
| 中性色 | `#303133` / `#606266` / `#909399` | 文本三层次 |
| 圆角 | 4px / 8px / 12px | 按组件大小适配 |
| 阴影 | `0 2px 12px rgba(0,0,0,0.1)` | 卡片与弹层 |
| 图标 | Font Awesome 6.5 | 统一的图标风格 |
| 动画 | `cubic-bezier(0.645, 0.045, 0.355, 1)` | 流畅自然 |

---

## 模块疆域

```
drp-platform/
├── drp-common          # 公共模块：通用响应封装、异常定义、常量枚举
├── drp-user-api        # 用户 API 契约：Feign 接口定义，跨模块通信的"宪法"
├── drp-auth            # 认证服务：JWT 令牌颁发、用户注册、角色分配
├── drp-project         # 项目服务：项目管理、成员管理、凭证中心
├── drp-server         # 服务器服务：服务器管理、分组管理、SSH 连接测试、心跳检测
├── drp-sql             # SQL 脚本：数据库初始化与迁移 DDL
└── drp-boot            # 聚合服务：整合所有模块，统一入口

drp-ui/
├── src/
│   ├── api/            # 统一 HTTP 层，封装所有后端接口
│   ├── components/     # 公共组件：Header、Dialog 等可复用组件
│   ├── views/          # 页面组件，Vue SFC 的艺术呈现
│   ├── router/         # 路由配置，SPA 导航的蓝图
│   ├── stores/         # Pinia 状态树，数据流的中枢
│   └── types/          # TypeScript 类型定义，前端的"契约"
└── vite.config.ts      # Vite 构建配置
```

### 模块职责矩阵

| 模块 | 职责边界 | 暴露接口 |
|------|---------|---------|
| drp-common | 公共代码、工具类、响应封装 | 被所有模块依赖 |
| drp-user-api | 用户服务接口定义 | 被 drp-auth、drp-project 依赖 |
| drp-auth | 用户认证、角色权限、JWT 令牌 | REST API + Feign |
| drp-project | 项目全生命周期管理 | REST API |
| drp-server | 服务器管理、SSH 连接测试、心跳检测 | REST API |
| drp-sql | 数据库 DDL/DML 脚本 | SQL 文件 |
| drp-boot | 模块聚合、服务编排 | 无独立接口 |

---

## 核心功能

### 1. 项目管理

*每一个伟大的产品，都始于一个精心规划的项目。*

- 项目 CRUD：支持 JAVA_MAVEN / NODE / PYTHON 多语言项目
- Git 仓库关联：SSH / HTTPS 双协议支持
- 凭证中心：敏感信息（Token、SSH Key）加密存储，按需调用
- 构建配置JSON化：差异化参数灵活扩展

### 2. 成员管理

*团队的力量，源于分工与协作的艺术。*

- 三级角色体系：OWNER（所有者）/ DEVELOPER（开发者）/ REPORTER（观察者）
- 细粒度权限控制：读/写/管三权分立
- 成员操作审计：谁在何时做了什么，全程可追溯

### 3. 环境变量

*配置与代码分离，是工程化思想的体现。*

- 多环境支持：dev / test / prod 三环境隔离
- 敏感变量加密：密码不上传，Security First
- 变量引用：支持 `${VAR_NAME}` 语法，DRY 原则的践行

### 4. 分支策略

*代码分支，是团队协作的航道。*

- 命名规范强制：通过正则表达式定义分支命名规则
- 保护分支：master/main 分支的守护者
- 自动化流水线：触发规则 → 自动构建 → 智能部署

### 5. 服务器管理

*精准掌控每一台服务器，让部署有的放矢。*

- 服务器 CRUD：支持多环境分组（dev/test/prod）
- 灵活认证：密码认证 + SSH 私钥双模式，敏感信息 AES 加密存储
- 连接测试：一键验证服务器连通性，快速排查问题
- 心跳检测：60 秒定时巡检，实时感知服务器在线状态
- 标签系统：自由标记、灵活筛选

---

## API 设计

### 认证服务 `/api/auth`

```
POST   /api/auth/login        # 用户登录，获取 JWT 令牌
POST   /api/auth/register     # 用户注册
POST   /api/auth/refresh     # 刷新 Access Token
POST   /api/auth/logout      # 注销会话
```

### 用户服务 `/api/users`

```
GET    /api/users                  # 分页查询用户
GET    /api/users/{id}            # 获取用户详情
GET    /api/users/simple-list      # 下拉选项（简化字段）
POST   /api/users                  # 创建用户
PUT    /api/users/{id}             # 更新用户
DELETE /api/users/{id}             # 删除用户
```

### 项目服务 `/api/projects`

```
GET    /api/projects                         # 分页查询项目
GET    /api/projects/{id}                   # 项目详情
POST   /api/projects                         # 创建项目
PUT    /api/projects/{id}                   # 更新项目
DELETE /api/projects/{id}                   # 删除项目
GET    /api/projects/git/branches             # 获取 Git 分支列表
GET    /api/projects/{id}/members            # 项目成员列表
POST   /api/projects/{id}/members            # 添加成员
PUT    /api/projects/{id}/members/{userId}   # 更新成员角色
DELETE /api/projects/{id}/members/{userId}   # 移除成员
```

### 服务器服务 `/api/servers`

```
GET    /api/servers                    # 分页查询服务器
GET    /api/servers/all                # 获取所有服务器
GET    /api/servers/{id}              # 服务器详情
POST   /api/servers                    # 创建服务器
PUT    /api/servers/{id}               # 更新服务器
DELETE /api/servers/{id}               # 删除服务器
POST   /api/servers/{id}/test          # 测试服务器连接
```

### 服务器分组 `/api/server-groups`

```
GET    /api/server-groups              # 获取所有分组
GET    /api/server-groups/{id}        # 分组详情
POST   /api/server-groups              # 创建分组
PUT    /api/server-groups/{id}        # 更新分组
DELETE /api/server-groups/{id}        # 删除分组
```

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1713696000000,
  "success": true
}
```

---

## 快速启航

### 环境清单

| 环境 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | Java 17 LTS，AMRA 的选择 |
| Maven | 3.8+ | 构建工具 |
| Node.js | 18+ | 前端运行时 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 6.0+ | 缓存与会话存储 |

### 后端部署

```bash
# 1. 克隆代码
git clone <repository-url>
cd drp-platform

# 2. 编译打包
mvn clean install -DskipTests

# 3. 初始化数据库
mysql -u root -p < drp-sql/src/main/sql/init/drp_init.sql

# 4. 修改配置
vim drp-boot/src/main/resources/application.yml
# 配置数据库连接、Redis 连接、JWT 密钥

# 5. 启动服务
cd drp-boot
mvn spring-boot:run
```

### 前端部署

```bash
# 1. 进入前端目录
cd drp-ui

# 2. 安装依赖
npm install

# 3. 开发模式
npm run dev        # 开发服务器：http://localhost:5173

# 4. 生产构建
npm run build      # 产物输出至 dist/
```

### Docker 部署（规划中）

```yaml
# docker-compose.yml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
  redis:
    image: redis:7-alpine
  drp-boot:
    build: ./drp-platform
    depends_on:
      - mysql
      - redis
```

---

## 工程实践

### 分支管理策略

```
main          ────── 生产环境分支，代码的唯一真源
   │
   ├── develop        ──── 开发主分支，功能的汇聚地
   │      │
   │      ├── feature/xxx   ──── 功能分支，PR 合并后销毁
   │      ├── bugfix/xxx    ──── 热修复分支
   │      └── refactor/xxx  ──── 重构分支，不影响功能
   │
   └── release/x.x.x ──── 发布分支，标签归档
```

### Git 提交规范

```
<type>(<scope>): <subject>

# 类型标识
feat:     新功能
fix:      缺陷修复
docs:     文档更新
style:    代码格式（不影响功能）
refactor: 重构（既不是新功能也不是修复）
test:     测试相关
chore:    构建/工具链变更
```

---

## 愿景

> *"Tools shape thinking, and thinking shapes code."*

DRP 不仅仅是一个 DevOps 平台，更是团队工程化能力的载体。我们相信：
- **透明**带来信任
- **自动化**带来效率
- **标准化**带来协作

让每一次发布，都成为团队值得骄傲的杰作，yeah！！！nice

---

## 许可证

Private Project - All Rights Reserved

---

*DRP - 让交付成为一种艺术*
