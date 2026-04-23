-- =====================================================
-- 构建模块数据库表
-- DRP Platform - Build Module
-- =====================================================

-- 1. 流水线配置表
CREATE TABLE IF NOT EXISTS `bld_pipeline` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水线ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `name` VARCHAR(100) NOT NULL COMMENT '流水线名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '流水线描述',

    -- 阶段配置 (JSON 格式)
    -- 结构: [{"name": "checkout", "type": "GIT_CLONE"}, {"name": "build", "type": "MAVEN_BUILD", "config": {"goals": "clean package"}}, ...]
    `stages` JSON NOT NULL COMMENT '阶段配置(JSON数组)',

    -- 构建参数 (JSON 格式)
    -- 结构: {"jdk": "17", "maven": "3.8.6", "node": "18.x"}
    `build_params` JSON DEFAULT NULL COMMENT '构建参数(JSON对象)',

    -- 触发配置
    `trigger_on_push` TINYINT DEFAULT 1 COMMENT '代码推送时触发',
    `trigger_on_tag` TINYINT DEFAULT 0 COMMENT '标签创建时触发',
    `trigger_on_schedule` TINYINT DEFAULT 0 COMMENT '定时触发',
    `cron_expression` VARCHAR(50) DEFAULT NULL COMMENT 'Cron 表达式',

    -- 超时配置 (秒)
    `timeout` INT DEFAULT 3600 COMMENT '超时时间(秒)',

    -- 状态
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否为默认流水线',

    -- 审计字段
    `create_user` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    UNIQUE KEY `uk_project_default` (`project_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流水线配置表';

-- 2. 构建记录表
CREATE TABLE IF NOT EXISTS `bld_build_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '构建记录ID',
    `build_number` INT NOT NULL COMMENT '构建号(项目内自增)',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `pipeline_id` BIGINT DEFAULT NULL COMMENT '关联流水线ID',

    -- Git 信息
    `branch` VARCHAR(100) NOT NULL COMMENT '构建分支',
    `commit_id` VARCHAR(40) NOT NULL COMMENT 'Commit ID',
    `commit_message` VARCHAR(500) DEFAULT NULL COMMENT 'Commit 消息',

    -- 构建配置快照
    `build_config` JSON DEFAULT NULL COMMENT '构建时使用的配置快照',

    -- 执行信息
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-排队, 1-运行中, 2-成功, 3-失败, 4-取消, 5-超时',
    `trigger_type` VARCHAR(20) NOT NULL COMMENT '触发类型：MANUAL, PUSH, TAG, SCHEDULE, API',
    `trigger_user` VARCHAR(100) DEFAULT NULL COMMENT '触发用户',

    -- 执行结果
    `duration` INT DEFAULT NULL COMMENT '构建耗时(秒)',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',

    -- 产物信息 (JSON 数组)
    `artifacts` JSON DEFAULT NULL COMMENT '构建产物列表',

    -- 执行服务器
    `executor_server` VARCHAR(100) DEFAULT NULL COMMENT '执行服务器地址',

    -- 审计字段
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_status` (`status`),
    KEY `idx_branch` (`branch`),
    KEY `idx_trigger_user` (`trigger_user`),
    KEY `idx_create_time` (`create_time`),
    UNIQUE KEY `uk_project_build_number` (`project_id`, `build_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='构建记录表';

-- 3. 构建产物表 (可选，详细记录产物信息)
CREATE TABLE IF NOT EXISTS `bld_artifact` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '产物ID',
    `build_record_id` BIGINT NOT NULL COMMENT '构建记录ID',
    `name` VARCHAR(200) NOT NULL COMMENT '产物名称',
    `path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    `download_url` VARCHAR(500) DEFAULT NULL COMMENT '下载URL',
    `checksum` VARCHAR(64) DEFAULT NULL COMMENT 'SHA256 校验码',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`id`),
    KEY `idx_build_record_id` (`build_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='构建产物表';

-- 4. 构建日志表 (可选，用于持久化日志)
CREATE TABLE IF NOT EXISTS `bld_build_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `build_record_id` BIGINT NOT NULL COMMENT '构建记录ID',
    `content` LONGTEXT NOT NULL COMMENT '日志内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`id`),
    KEY `idx_build_record_id` (`build_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='构建日志表';

-- 5. 项目构建号序列表 (用于生成项目内自增的构建号)
CREATE TABLE IF NOT EXISTS `bld_build_sequence` (
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `current_value` INT NOT NULL DEFAULT 0 COMMENT '当前值',
    PRIMARY KEY (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='构建号序列表';

-- =====================================================
-- 初始数据
-- =====================================================

-- 插入示例流水线配置 (drp-api 项目)
INSERT INTO `bld_pipeline` (`project_id`, `name`, `description`, `stages`, `build_params`, `trigger_on_push`, `timeout`, `status`, `is_default`, `create_time`) VALUES
(1, '默认流水线', 'Java Maven 项目默认构建流水线', '[{"name":"checkout","type":"GIT_CLONE","enabled":true},{"name":"compile","type":"MAVEN_BUILD","enabled":true,"config":{"goals":"compile"}},{"name":"test","type":"MAVEN_BUILD","enabled":true,"config":{"goals":"test"}},{"name":"package","type":"MAVEN_BUILD","enabled":true,"config":{"goals":"package -DskipTests"}}]', '{"jdk":"17","maven":"3.8.6"}', 1, 3600, 1, 1, NOW());

-- 插入示例流水线配置 (drp-web 项目)
INSERT INTO `bld_pipeline` (`project_id`, `name`, `description`, `stages`, `build_params`, `trigger_on_push`, `timeout`, `status`, `is_default`, `create_time`) VALUES
(2, '前端构建', 'Node.js 前端项目构建流水线', '[{"name":"checkout","type":"GIT_CLONE","enabled":true},{"name":"install","type":"NPM_BUILD","enabled":true,"config":{"command":"install"}},{"name":"build","type":"NPM_BUILD","enabled":true,"config":{"command":"run build"}}]', '{"node":"18.x","npm":"9.x"}', 1, 1800, 1, 1, NOW());
