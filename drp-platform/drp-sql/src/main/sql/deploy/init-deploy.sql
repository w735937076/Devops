-- ============================================
-- 部署服务模块 SQL 初始化脚本
-- ============================================

CREATE TABLE IF NOT EXISTS `dpl_deploy_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部署记录ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `build_id` BIGINT NOT NULL COMMENT '构建记录ID',
    `env` VARCHAR(20) NOT NULL COMMENT '部署环境: dev/test/pre/prod',
    `strategy` VARCHAR(30) NOT NULL COMMENT '部署策略',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0待审批 1排队 2执行中 3成功 4失败 5已取消 6已回滚 7已驳回',
    `approval_status` VARCHAR(20) NOT NULL DEFAULT 'NOT_REQUIRED' COMMENT '审批状态',
    `risk_level` VARCHAR(10) NOT NULL DEFAULT 'LOW' COMMENT '风险等级',
    `trigger_type` VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '触发方式',
    `trigger_user` VARCHAR(100) DEFAULT NULL COMMENT '触发用户',
    `version` VARCHAR(100) NOT NULL COMMENT '版本号',
    `branch` VARCHAR(100) DEFAULT NULL COMMENT '分支',
    `server_ids` VARCHAR(500) DEFAULT NULL COMMENT '服务器ID列表(csv)',
    `server_snapshot` JSON DEFAULT NULL COMMENT '服务器快照(JSON)',
    `deploy_window` VARCHAR(50) DEFAULT NULL COMMENT '发布窗口',
    `gray_config` JSON DEFAULT NULL COMMENT '灰度参数',
    `change_ticket` VARCHAR(100) DEFAULT NULL COMMENT '变更单号',
    `window_valid` TINYINT DEFAULT 1 COMMENT '是否命中发布窗口',
    `auto_rollback` TINYINT DEFAULT 1 COMMENT '是否自动回滚',
    `current_step` VARCHAR(50) DEFAULT NULL COMMENT '当前阶段',
    `duration` INT DEFAULT NULL COMMENT '耗时(秒)',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `rollback_from_deploy_id` BIGINT DEFAULT NULL COMMENT '回滚来源部署ID',
    `log_content` LONGTEXT DEFAULT NULL COMMENT '部署日志',
    `health_checks` JSON DEFAULT NULL COMMENT '健康检查结果(JSON)',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_env_status` (`project_id`, `env`, `status`),
    KEY `idx_build_id` (`build_id`),
    KEY `idx_approval_status` (`approval_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部署记录表';

CREATE TABLE IF NOT EXISTS `dpl_deploy_step` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '步骤ID',
    `deploy_id` BIGINT NOT NULL COMMENT '部署ID',
    `step_key` VARCHAR(50) NOT NULL COMMENT '步骤编码',
    `step_name` VARCHAR(100) NOT NULL COMMENT '步骤名称',
    `step_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    `duration` INT DEFAULT NULL COMMENT '耗时(秒)',
    `detail` VARCHAR(500) DEFAULT NULL COMMENT '步骤说明',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_deploy_id` (`deploy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部署阶段表';

CREATE TABLE IF NOT EXISTS `dpl_approval_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审批记录ID',
    `deploy_id` BIGINT NOT NULL COMMENT '部署ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `env` VARCHAR(20) NOT NULL COMMENT '环境',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态',
    `approver` VARCHAR(100) DEFAULT NULL COMMENT '审批人',
    `comment` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_deploy_id` (`deploy_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部署审批记录表';
