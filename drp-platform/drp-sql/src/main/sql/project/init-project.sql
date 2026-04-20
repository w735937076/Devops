-- 1. 凭证管理表：统一管理 Git Token、SSH 私钥等敏感信息
CREATE TABLE IF NOT EXISTS `sys_credentials` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '凭证ID',
    `name` VARCHAR(100) NOT NULL COMMENT '凭证名称(如：公司主Git账号)',
    `type` VARCHAR(20) NOT NULL COMMENT '类型：USERNAME_PASSWORD, ACCESS_TOKEN, SSH_KEY',
    `account` VARCHAR(100) DEFAULT NULL COMMENT '账号/标识',
    `secret_content` TEXT NOT NULL COMMENT '加密存储的密码、Token或私钥',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='凭证管理表';

-- 2. 项目主表：核心配置优化
CREATE TABLE IF NOT EXISTS `prj_project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `code` VARCHAR(50) NOT NULL COMMENT '项目编码(用于Linux目录名，全局唯一)',
    `type` VARCHAR(20) NOT NULL COMMENT '类型：JAVA_MAVEN, NODE, PYTHON',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '项目描述',

    -- Git 配置优化：关联凭证中心
    `git_url` VARCHAR(500) NOT NULL COMMENT 'Git仓库地址',
    `credential_id` BIGINT DEFAULT NULL COMMENT '关联凭证ID',
    `default_branch` VARCHAR(100) DEFAULT 'master' COMMENT '默认构建分支',

    -- 构建配置优化：使用JSON存储不同语言的差异化参数
    -- 例如：{"jdk": "17", "maven": "3.8"} 或 {"node": "18.x"}
    `build_config` JSON DEFAULT NULL COMMENT '构建参数配置(JSON格式)',

    -- 状态与审计
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    `version` INT DEFAULT 1 COMMENT '乐观锁版本号',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB COMMENT='项目基础信息表';

-- 3. 项目成员表：权限细化预留
CREATE TABLE IF NOT EXISTS `prj_project_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL COMMENT '项目角色：OWNER, DEVELOPER, REPORTER',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_prj_user` (`project_id`, `user_id`)
) ENGINE=InnoDB COMMENT='项目成员关联表';

-- 4. 环境变量表：增加脱敏与环境标识
CREATE TABLE IF NOT EXISTS `prj_env_variable` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `env_code` VARCHAR(20) NOT NULL COMMENT '环境：dev, test, prod',
    `var_key` VARCHAR(100) NOT NULL COMMENT '变量Key',
    `var_value` TEXT NOT NULL COMMENT '变量值(密文存储)',
    `is_secret` TINYINT DEFAULT 0 COMMENT '是否脱敏显示：0-否, 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_project_env` (`project_id`, `env_code`)
) ENGINE=InnoDB COMMENT='项目环境变量表';

-- 5. 分支发布策略表
CREATE TABLE IF NOT EXISTS `prj_branch_policy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `branch_pattern` VARCHAR(100) NOT NULL COMMENT '分支匹配正则(如: release/*)',
    `allow_auto_deploy` TINYINT DEFAULT 0 COMMENT '是否允许自动触发',
    `require_approval` TINYINT DEFAULT 0 COMMENT '是否需要人工审批',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='项目分支发布策略表';