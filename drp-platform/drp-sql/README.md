# DRP SQL 模块

集中管理 DRP 平台所有数据库脚本。

## 目录结构

```
drp-sql/src/main/sql/
├── auth/                    # 认证模块 SQL
│   └── init-auth.sql       # 初始化脚本（包含表结构和初始数据）
└── project/                # 项目管理模块 SQL
    └── init-project.sql    # 初始化脚本（包含表结构）
```

## 使用方式

### 方式一：手动执行 SQL 文件

```bash
# 登录 MySQL
mysql -u root -p

# 执行认证模块初始化
source drp-sql/src/main/sql/auth/init-auth.sql

# 执行项目管理模块初始化
source drp-sql/src/main/sql/project/init-project.sql
```

### 方式二：命令行直接执行

```bash
mysql -u root -p drp < drp-sql/src/main/sql/auth/init-auth.sql
mysql -u root -p drp < drp-sql/src/main/sql/project/init-project.sql
```

## 模块说明

### auth 模块
- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_permission` - 权限表
- `sys_user_role` - 用户角色关联表
- `sys_role_permission` - 角色权限关联表
- `sys_user_permission` - 用户权限关联表

### project 模块
- `prj_project` - 项目表
- `prj_project_member` - 项目成员表
- `prj_env_variable` - 环境变量表
- `prj_build` - 构建记录表
- `prj_deploy` - 部署记录表
- `prj_server` - 服务器表

## 注意事项

1. **执行顺序**：先执行 `auth` 模块，再执行 `project` 模块
2. **数据库**：确保 `drp` 数据库已创建
3. **初始密码**：`admin` 用户的默认密码为 `admin123`（BCrypt 加密）
