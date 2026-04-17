-- =====================================================
-- DRP 平台认证模块数据库初始化脚本
-- =====================================================
-- 创建数据库 (如果不存在)
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS drp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE drp;

-- =====================================================
-- 1. 用户表 (sys_user)
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(100) COMMENT '真实姓名',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 角色表 (sys_role)
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) COMMENT '角色描述',
    `type` VARCHAR(20) NOT NULL DEFAULT 'CUSTOM' COMMENT '角色类型: SYSTEM-系统角色 CUSTOM-自定义角色',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` BIGINT COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =====================================================
-- 3. 权限表 (sys_permission)
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `description` VARCHAR(255) COMMENT '权限描述',
    `parent_id` BIGINT COMMENT '父权限ID',
    `type` VARCHAR(20) NOT NULL DEFAULT 'API' COMMENT '权限类型: MENU-菜单 BUTTON-按钮 API-接口',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `icon` VARCHAR(50) COMMENT '图标',
    `path` VARCHAR(200) COMMENT '路由路径',
    `component` VARCHAR(200) COMMENT '组件路径',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` BIGINT COMMENT '更新人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- =====================================================
-- 4. 用户角色关联表 (sys_user_role)
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT COMMENT '创建人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =====================================================
-- 5. 角色权限关联表 (sys_role_permission)
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT COMMENT '创建人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =====================================================
-- 6. 用户权限关联表 (sys_user_permission) - 可选，直接给用户分配权限
-- =====================================================
CREATE TABLE IF NOT EXISTS `sys_user_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT COMMENT '创建人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_permission` (`user_id`, `permission_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限关联表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入默认管理员角色
INSERT INTO `sys_role` (`code`, `name`, `description`, `type`, `sort`, `status`) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限', 'SYSTEM', 1, 1),
('DEVELOPER', '开发人员', '开发人员角色', 'SYSTEM', 2, 1),
('VIEWER', '查看者', '只读权限', 'SYSTEM', 3, 1);

-- 插入默认管理员用户 (密码: admin123)
-- BCrypt 加密后的密码: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin@drp.com', 1),
('dev', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '开发者', 'dev@drp.com', 1);

-- 将 admin 用户关联到 ADMIN 角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2);

-- 插入默认权限
INSERT INTO `sys_permission` (`code`, `name`, `description`, `type`, `sort`, `status`) VALUES
-- 项目权限
('PROJECT_VIEW', '查看项目', '查看项目列表和详情', 'API', 1, 1),
('PROJECT_CREATE', '创建项目', '创建新项目', 'API', 2, 1),
('PROJECT_EDIT', '编辑项目', '编辑项目信息', 'API', 3, 1),
('PROJECT_DELETE', '删除项目', '删除项目', 'API', 4, 1),
-- 构建权限
('BUILD_VIEW', '查看构建', '查看构建记录', 'API', 10, 1),
('BUILD_TRIGGER', '触发构建', '触发新的构建', 'API', 11, 1),
('BUILD_CANCEL', '取消构建', '取消正在进行的构建', 'API', 12, 1),
-- 部署权限
('DEPLOY_VIEW', '查看部署', '查看部署记录', 'API', 20, 1),
('DEPLOY_EXECUTE', '执行部署', '执行部署操作', 'API', 21, 1),
('DEPLOY_ROLLBACK', '回滚部署', '执行回滚操作', 'API', 22, 1),
('DEPLOY_PROD', '生产部署', '在生产环境执行部署', 'API', 23, 1),
-- 服务器权限
('SERVER_VIEW', '查看服务器', '查看服务器列表', 'API', 30, 1),
('SERVER_CREATE', '创建服务器', '添加新服务器', 'API', 31, 1),
('SERVER_EDIT', '编辑服务器', '编辑服务器信息', 'API', 32, 1),
('SERVER_DELETE', '删除服务器', '删除服务器', 'API', 33, 1),
-- 系统权限
('USER_MANAGE', '用户管理', '管理系统用户', 'API', 40, 1),
('ROLE_MANAGE', '角色管理', '管理角色和权限', 'API', 41, 1),
('AUDIT_VIEW', '审计日志', '查看审计日志', 'API', 42, 1);

-- 将所有权限关联到 ADMIN 角色
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- 将部分权限关联到 DEVELOPER 角色
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, id FROM `sys_permission` WHERE `code` IN (
    'PROJECT_VIEW', 'PROJECT_CREATE', 'PROJECT_EDIT',
    'BUILD_VIEW', 'BUILD_TRIGGER', 'BUILD_CANCEL',
    'DEPLOY_VIEW', 'DEPLOY_EXECUTE',
    'SERVER_VIEW'
);
