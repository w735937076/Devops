-- =====================================================
-- 日志模块数据库表
-- DRP Platform - Log Module
-- =====================================================

-- 1. 操作日志表
CREATE TABLE IF NOT EXISTS `log_operation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `operator` VARCHAR(50) NOT NULL COMMENT '操作人',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：BUILD, DEPLOY, ROLLBACK, CREATE, UPDATE, DELETE',
    `project_id` BIGINT COMMENT '项目ID',
    `project_name` VARCHAR(100) COMMENT '项目名称',
    `env` VARCHAR(20) COMMENT '环境',
    `version` VARCHAR(100) COMMENT '版本号',
    `detail` JSON COMMENT '详细信息',
    `ip` VARCHAR(50) COMMENT '操作人IP',
    `status` VARCHAR(20) NOT NULL COMMENT '状态: SUCCESS, FAIL',
    `duration` BIGINT COMMENT '耗时(毫秒)',
    `error_message` TEXT COMMENT '错误信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =====================================================
-- 索引说明
-- =====================================================
-- idx_operator_id: 按操作人查询
-- idx_project_id: 按项目查询
-- idx_create_time: 按时间范围查询
-- idx_operation_type: 按操作类型查询
-- idx_status: 按状态查询

-- =====================================================
-- 操作类型说明
-- =====================================================
-- BUILD: 构建
-- DEPLOY: 部署
-- ROLLBACK: 回滚
-- CREATE: 创建
-- UPDATE: 更新
-- DELETE: 删除

-- =====================================================
-- 初始数据 (可选，用于测试)
-- =====================================================

-- INSERT INTO `log_operation` (`operator`, `operator_id`, `operation_type`, `project_id`, `project_name`, `env`, `status`, `duration`, `ip`, `create_time`) VALUES
-- ('admin', 1, 'BUILD', 1, 'drp-api', 'dev', 'SUCCESS', 120000, '192.168.1.100', NOW());
