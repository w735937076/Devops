# DRP Platform 前端基础框架开发文档

## 文档信息

| 字段 | 内容 |
|-----|------|
| 项目名称 | DRP DevOps Platform |
| 文档版本 | 1.0.0 |
| 作者 | Nick |
| 创建日期 | 2026-04-18 |

---

## 1. 项目概述

DRP Platform 前端是基于 Vue 3 + Vite + TypeScript 的现代化前端项目，采用 Element Plus 作为 UI 组件库，Pinia 作为状态管理，Vue Router 作为路由管理。本框架为 DRP DevOps 平台提供基础设施支持，包括项目结构、路由配置、状态管理、布局组件、样式规范等。

### 1.1 技术栈

| 技术 | 版本 | 说明 |
|-----|------|------|
| Vue | 3.4+ | 核心框架 |
| Vite | 5.x | 构建工具 |
| TypeScript | 5.x | 类型支持 |
| Element Plus | 2.5+ | UI组件库 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.6+ | HTTP客户端 |
| SCSS | - | 样式预处理 |

---

## 2. 项目结构

```
drp-ui/
├── public/
│   └── favicon.svg              # 网站图标
├── src/
│   ├── api/                    # API封装
│   │   ├── request.ts          # Axios封装 (核心)
│   │   ├── auth.ts            # 认证API
│   │   ├── project.ts         # 项目API
│   │   ├── build.ts           # 构建API
│   │   ├── deploy.ts          # 部署API
│   │   ├── server.ts          # 服务器API
│   │   └── index.ts           # API导出
│   │
│   ├── assets/
│   │   ├── styles/
│   │   │   ├── variables.scss # 全局变量
│   │   │   ├── mixins.scss    # 混合宏
│   │   │   └── global.scss    # 全局样式
│   │   └── images/            # 图片资源
│   │
│   ├── components/             # 公共组件
│   │   ├── common/
│   │   │   ├── DrpTable.vue   # 表格组件
│   │   │   ├── DrpForm.vue    # 表单组件
│   │   │   ├── DrpDialog.vue   # 弹窗组件
│   │   │   └── DrpPage.vue     # 分页组件
│   │   ├── layout/
│   │   │   ├── AppSidebar.vue  # 侧边栏
│   │   │   ├── AppHeader.vue   # 顶部导航
│   │   │   └── AppBreadcrumb.vue # 面包屑
│   │   └── log/
│   │       └── LogViewer.vue   # 日志查看器
│   │
│   ├── composables/            # 组合式函数 (预留)
│   │
│   ├── layouts/
│   │   ├── DefaultLayout.vue   # 默认布局
│   │   ├── BlankLayout.vue     # 空白布局
│   │   └── FullscreenLayout.vue # 全屏布局
│   │
│   ├── router/
│   │   ├── index.ts           # 路由实例
│   │   ├── routes.ts          # 路由配置
│   │   └── guards.ts          # 路由守卫
│   │
│   ├── stores/                 # 状态管理
│   │   ├── user.ts            # 用户状态
│   │   ├── project.ts         # 项目状态
│   │   └── app.ts             # 应用状态
│   │
│   ├── utils/                 # 工具函数
│   │   ├── storage.ts         # 本地存储
│   │   ├── validate.ts        # 表单验证
│   │   ├── date.ts            # 日期格式化
│   │   └── color.ts           # 颜色工具
│   │
│   ├── views/                 # 页面组件
│   │   ├── auth/
│   │   │   └── Login.vue      # 登录页
│   │   ├── dashboard/
│   │   │   └── Index.vue      # 首页
│   │   ├── project/
│   │   ├── build/
│   │   ├── deploy/
│   │   ├── server/
│   │   ├── log/
│   │   ├── system/
│   │   └── error/
│   │       ├── 404.vue        # 404页面
│   │       └── 403.vue        # 403页面
│   │
│   ├── App.vue                # 根组件
│   ├── main.ts                # 入口文件
│   └── env.d.ts               # 类型声明
│
├── index.html
├── vite.config.ts
├── tsconfig.json
├── tsconfig.node.json
├── package.json
└── .env
```

---

## 3. 核心模块详解

### 3.1 Axios 请求封装

**文件位置**: `src/api/request.ts`

#### 功能特性

- **统一拦截请求/响应**: 自动处理请求和响应错误
- **Token 自动附加**: 自动在请求头中添加 Bearer Token
- **Token 过期自动刷新**: 401 时自动尝试刷新 Token
- **统一错误处理**: 区分不同类型的错误并展示友好提示
- **请求队列管理**: 避免多个请求同时刷新 Token

#### 核心实现

```typescript
// 请求拦截器
service.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

// 响应拦截器 - Token 刷新机制
let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

// 401 处理流程:
// 1. 尝试刷新 Token
// 2. 刷新成功则重试原请求
// 3. 刷新失败则跳转登录页
```

#### 导出方法

```typescript
export function get<T>(url: string, params?: any, config?: RequestConfig): Promise<T>
export function post<T>(url: string, data?: any, config?: RequestConfig): Promise<T>
export function put<T>(url: string, data?: any, config?: RequestConfig): Promise<T>
export function del<T>(url: string, params?: any, config?: RequestConfig): Promise<T>
export function patch<T>(url: string, data?: any, config?: RequestConfig): Promise<T>
```

---

### 3.2 路由配置

**文件位置**: `src/router/`

#### 路由配置

```typescript
// routes.ts
const routes = [
  // 公开路由
  { path: '/login', meta: { public: true } },
  // 业务路由（需登录）
  {
    path: '/',
    component: DefaultLayout,
    children: [
      { path: 'dashboard', component: Dashboard },
      // ...
    ]
  }
]
```

#### 路由守卫

```typescript
// guards.ts
router.beforeEach((to, from, next) => {
  // 1. 设置页面标题
  document.title = `${to.meta.title} - DRP`

  // 2. 检查白名单
  if (whiteList.includes(to.path)) {
    return next()
  }

  // 3. 检查登录状态
  if (hasToken) {
    // 4. 验证用户信息
    if (!userInfo) {
      fetchUserInfo().catch(() => logout())
    }
    next()
  } else {
    next('/login')
  }
})
```

---

### 3.3 状态管理

**文件位置**: `src/stores/`

#### 用户状态 (user.ts)

```typescript
export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('accessToken') || '')
  const userInfo = ref<UserInfo | null>(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const hasPermission = computed(() => (perm: string) => permissions.value.includes(perm))

  // Actions
  async function login(params) { /* ... */ }
  async function logout() { /* ... */ }
  async function fetchUserInfo() { /* ... */ }

  return { token, userInfo, isLoggedIn, hasPermission, login, logout }
})
```

#### 应用状态 (app.ts)

```typescript
export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const sidebarWidth = computed(() => collapsed ? 64 : 200)
  const isMobile = ref(window.innerWidth < 768)

  function toggleSidebar() { /* ... */ }
  function handleResize() { /* ... */ }

  return { sidebarCollapsed, sidebarWidth, isMobile, toggleSidebar }
})
```

---

### 3.4 布局组件

**文件位置**: `src/components/layout/`

#### DefaultLayout.vue

主布局组件，包含侧边栏、顶部导航、面包屑和主内容区。

```vue
<template>
  <el-container class="default-layout">
    <el-aside :width="sidebarWidth">
      <AppSidebar />
    </el-aside>
    <el-container>
      <el-header>
        <AppHeader />
      </el-header>
      <div class="breadcrumb-container">
        <AppBreadcrumb />
      </div>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
```

#### AppSidebar.vue

侧边栏组件，支持：
- 动态菜单渲染
- 菜单高亮
- 折叠/展开动画
- 响应式适配

#### AppHeader.vue

顶部导航组件，包含：
- 页面标题
- 用户信息下拉菜单
- 快捷操作按钮

---

### 3.5 登录页面

**文件位置**: `src/views/auth/Login.vue`

#### 功能特性

- 渐变背景装饰
- 响应式设计
- 表单验证
- 登录状态管理
- 错误提示

#### 核心代码

```typescript
async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    // 错误已由 Axios 拦截器处理
  } finally {
    loading.value = false
  }
}
```

---

### 3.6 日志查看器

**文件位置**: `src/components/log/LogViewer.vue`

#### 功能特性

- **日志高亮**: 根据日志级别（ERROR/WARN/INFO/DEBUG）显示不同颜色
- **关键字搜索**: 实时过滤并高亮匹配内容
- **自动滚动**: 新日志自动滚动到底部
- **日志下载**: 支持导出为文本文件
- **全屏模式**: 支持全屏查看
- **ANSI 颜色解析**: 支持解析终端颜色代码

#### Props

| 属性 | 类型 | 默认值 | 说明 |
|-----|------|-------|------|
| logs | string[] | [] | 日志数组 |
| height | string | '400px' | 组件高度 |
| autoScroll | boolean | true | 是否自动滚动 |

---

## 4. 工具函数

### 4.1 存储工具 (storage.ts)

```typescript
// 存储
storage.set('key', value)
storage.get('key')
storage.remove('key')
storage.clear()

// 便捷方法
setToken(token)
getToken()
removeToken()
```

### 4.2 表单验证 (validate.ts)

```typescript
// 内置验证器
validators.required('请输入内容')
validators.username()
validators.password()
validators.phone()
validators.email()
validators.ip()
validators.pattern(/正则/, '格式错误')

// 常用正则
patterns.username  // 4-16位字母数字下划线
patterns.phone    // 手机号
patterns.email    // 邮箱
patterns.url      // URL
patterns.ip       // IP地址
```

### 4.3 日期格式化 (date.ts)

```typescript
formatDate(date, 'YYYY-MM-DD')
formatDateTime(date)  // 'YYYY-MM-DD HH:mm:ss'
formatRelativeTime(date)  // '刚刚', '5分钟前', '2小时前'
formatDuration(ms)  // '1小时30分钟'
```

### 4.4 颜色工具 (color.ts)

```typescript
// 颜色转换
hexToRgb('#667eea')
rgbToHex(102, 126, 234)
setAlpha('#667eea', 0.5)

// 颜色操作
darken('#667eea', 20)  // 加深
lighten('#667eea', 20) // 变浅

// 状态颜色
getStatusColor('success')  // '#67c23a'
getStatusColor(2)  // 根据状态码获取
```

---

## 5. 样式规范

### 5.1 SCSS 变量

**文件位置**: `src/assets/styles/variables.scss`

```scss
// 主题色
$primary-color: #667eea;
$primary-light: #8b9fed;
$primary-dark: #5568d0;

// 状态色
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;
$info-color: #909399;

// 侧边栏
$sidebar-width: 200px;
$sidebar-collapsed-width: 64px;
$header-height: 60px;
```

### 5.2 全局样式

**文件位置**: `src/assets/styles/global.scss`

提供以下全局样式类：
- `.page-container`: 页面容器
- `.page-header`: 页面头部
- `.card-container`: 卡片容器
- `.status-tag`: 状态标签
- `.text-ellipsis`: 文字省略
- `.flex`, `.flex-center`, `.flex-between`: Flex 工具类

---

## 6. 开发命令

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览构建结果
npm run preview

# 代码格式化
npm run format

# 代码检查
npm run lint
```

---

## 7. 接口对接

### 7.1 环境配置

```bash
# .env
VITE_API_BASE_URL=/api
```

### 7.2 API 调用示例

```typescript
// 登录
import { login } from '@/api/auth'

const res = await login({ username: 'admin', password: '123456' })
userStore.setToken(res.accessToken)

// 获取列表
import { getProjectList } from '@/api/project'

const { list, total } = await getProjectList({ page: 1, pageSize: 10 })
```

---

## 8. 验收标准

| 验收项 | 标准 | 状态 |
|-------|------|------|
| npm install | 无错误 | ✅ |
| npm run dev | 启动成功 | ✅ |
| 登录页面 | 正常显示 | ✅ |
| 路由守卫 | 401 自动跳转登录 | ✅ |
| Token 处理 | 自动附加到请求头 | ✅ |
| 首页仪表盘 | 正常渲染 | ✅ |
| 日志组件 | 高亮/搜索/下载 | ✅ |

---

## 9. 待完善功能

以下功能需要在后续迭代中完善：

- [ ] 移动端适配优化
- [ ] 主题切换功能
- [ ] 全屏日志模式
- [ ] 国际化支持
- [ ] 前端 Mock 服务
- [ ] 单元测试

---

*文档结束*
