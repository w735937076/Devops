-- ============================================
-- 服务器管理模块 SQL 初始化脚本
-- ============================================

-- 服务器表
CREATE TABLE IF NOT EXISTS `srv_server` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '服务器ID',
    `name` VARCHAR(100) NOT NULL COMMENT '服务器名称',
    `hostname` VARCHAR(100) NOT NULL COMMENT '主机名或IP',
    `port` INT NOT NULL DEFAULT 22 COMMENT 'SSH端口',
    `username` VARCHAR(50) NOT NULL COMMENT 'SSH用户名',
    `password` TEXT COMMENT 'SSH密码(加密)',
    `private_key` TEXT COMMENT 'SSH私钥(加密)',
    `private_key_passphrase` TEXT COMMENT '私钥密码(加密)',
    `groups` VARCHAR(500) COMMENT '所属分组,逗号分隔',
    `work_dir` VARCHAR(255) DEFAULT '/opt/drp' COMMENT '工作目录',
    `backup_dir` VARCHAR(255) DEFAULT '/opt/drp/backup' COMMENT '备份目录',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0离线 1在线 2忙碌',
    `env` VARCHAR(20) COMMENT '环境: dev/test/prod',
    `tags` VARCHAR(255) COMMENT '标签，逗号分隔',
    `remark` TEXT COMMENT '备注',
    `last_heartbeat` DATETIME COMMENT '最后心跳时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_groups` (`groups`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器表';

-- 服务器分组表
CREATE TABLE IF NOT EXISTS `srv_server_group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分组名称',
    `code` VARCHAR(50) NOT NULL COMMENT '分组编码',
    `description` VARCHAR(255) COMMENT '分组描述',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器分组表';

-- 项目-服务器关联表
CREATE TABLE IF NOT EXISTS `prj_project_server` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `server_id` BIGINT NOT NULL COMMENT '服务器ID',
    `env` VARCHAR(20) NOT NULL COMMENT '环境',
    `deploy_path` VARCHAR(255) COMMENT '部署路径',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_server_env` (`project_id`, `server_id`, `env`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目服务器关联表';

-- 初始化服务器分组数据
INSERT INTO `srv_server_group` (`name`, `code`, `description`, `sort`) VALUES
('Web服务器', 'web', 'Web应用服务器', 1),
('API服务器', 'api', 'API服务服务器', 2),
('数据库服务器', 'db', '数据库服务器', 3),
('其他服务器', 'other', '其他用途服务器', 4);
