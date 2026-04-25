-- =====================================================
-- 通知模块数据库表
-- DRP Platform - Notify Module
-- 2026-04-26 更新
-- =====================================================

-- 1. 通知渠道表
CREATE TABLE IF NOT EXISTS `ntf_channel` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '渠道ID',
    `code` VARCHAR(20) NOT NULL COMMENT '渠道编码: WECOM, DINGTALK, FEISHU, EMAIL',
    `name` VARCHAR(50) NOT NULL COMMENT '渠道名称',
    `icon` VARCHAR(50) COMMENT '图标',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `config_schema` JSON COMMENT '配置Schema，用于前端渲染配置表单',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知渠道表';

-- 2. 通知配置表
CREATE TABLE IF NOT EXISTS `ntf_notification_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `project_id` BIGINT COMMENT '项目ID, NULL表示全局配置',
    `channel` VARCHAR(20) NOT NULL COMMENT '渠道: WECOM, DINGTALK, FEISHU, EMAIL',
    `event` VARCHAR(50) NOT NULL COMMENT '事件类型',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `config` JSON NOT NULL COMMENT '渠道配置',
    `recipients` VARCHAR(500) NOT NULL COMMENT '接收人',
    `template_id` BIGINT COMMENT '使用的模板ID',
    `rate_limit` INT DEFAULT 60 COMMENT '频率限制(秒)，0表示不限制',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_channel_event` (`channel`, `event`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知配置表';

-- 3. 通知记录表
CREATE TABLE IF NOT EXISTS `ntf_notification_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `config_id` BIGINT NOT NULL COMMENT '配置ID',
    `channel` VARCHAR(20) NOT NULL COMMENT '渠道',
    `event` VARCHAR(50) NOT NULL COMMENT '事件',
    `recipient` VARCHAR(200) NOT NULL COMMENT '接收人',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT COMMENT '内容',
    `status` VARCHAR(20) NOT NULL COMMENT '状态: PENDING, SUCCESS, FAIL, RETRY',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `max_retry` INT DEFAULT 3 COMMENT '最大重试次数',
    `error_message` TEXT COMMENT '错误信息',
    `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `sent_time` DATETIME COMMENT '实际发送时间',
    `read_time` DATETIME COMMENT '读取时间(仅邮件)',
    `external_id` VARCHAR(100) COMMENT '外部消息ID，用于追踪',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_config_id` (`config_id`),
    KEY `idx_status` (`status`),
    KEY `idx_send_time` (`send_time`),
    KEY `idx_event` (`event`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';

-- 4. 通知模板表
CREATE TABLE IF NOT EXISTS `ntf_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `channel` VARCHAR(20) NOT NULL COMMENT '适用渠道',
    `event` VARCHAR(50) NOT NULL COMMENT '适用事件',
    `title_template` VARCHAR(200) NOT NULL COMMENT '标题模板',
    `content_template` TEXT NOT NULL COMMENT '内容模板',
    `variables` JSON COMMENT '变量定义 [{name, desc, required}]',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_channel_event` (`channel`, `event`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知模板表';

-- 5. 通知频率控制表 (用于去重/合并)
CREATE TABLE IF NOT EXISTS `ntf_rate_control` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `project_id` BIGINT COMMENT '项目ID',
    `event` VARCHAR(50) NOT NULL COMMENT '事件类型',
    `channel` VARCHAR(20) NOT NULL COMMENT '渠道',
    `recipient` VARCHAR(200) NOT NULL COMMENT '接收人',
    `last_send_time` DATETIME NOT NULL COMMENT '最后发送时间',
    `send_count` INT DEFAULT 1 COMMENT '发送计数',
    `window_start` DATETIME NOT NULL COMMENT '窗口开始时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_event_channel_recipient` (`event`, `channel`, `recipient`, `project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知频率控制表';

-- =====================================================
-- 初始数据
-- =====================================================

-- 插入渠道信息
INSERT INTO `ntf_channel` (`code`, `name`, `icon`, `enabled`, `config_schema`) VALUES
('WECOM', '企业微信', 'fab fa-weixin', 1, '{"webhookUrl": {"type": "string", "label": "Webhook地址", "required": true}, "safe": {"type": "select", "label": "安全设置", "options": [{"label": "不加密", "value": 0}, {"label": "签名校验", "value": 1}], "default": 0}}'),
('DINGTALK', '钉钉', 'fas fa-bullhorn', 1, '{"webhookUrl": {"type": "string", "label": "Webhook地址", "required": true}, "secret": {"type": "string", "label": "加签密钥", "required": false}}'),
('FEISHU', '飞书', 'fas fa-egg', 1, '{"webhookUrl": {"type": "string", "label": "Webhook地址", "required": true}}'),
('EMAIL', '邮件', 'fas fa-envelope', 1, '{"smtpHost": {"type": "string", "label": "SMTP服务器", "required": true}, "smtpPort": {"type": "number", "label": "端口", "required": true, "default": 465}, "username": {"type": "string", "label": "用户名", "required": true}, "password": {"type": "password", "label": "密码", "required": true}, "from": {"type": "string", "label": "发件人", "required": false}, "useSsl": {"type": "boolean", "label": "使用SSL", "default": true}}');

-- 插入默认模板
INSERT INTO `ntf_template` (`name`, `channel`, `event`, `title_template`, `content_template`, `variables`, `enabled`) VALUES
('构建成功通知', 'WECOM', 'BUILD_SUCCESS', '[DRP] {{projectName}} 构建成功', '# {{projectName}} 构建成功\n\n版本: **{{version}}**\n分支: {{branch}}\n耗时: {{duration}}\n操作人: {{operator}}\n\n> 点击查看详情', '{"projectName": "项目名称", "version": "版本号", "branch": "分支", "duration": "耗时", "operator": "操作人"}', 1),
('构建失败告警', 'WECOM', 'BUILD_FAIL', '[DRP告警] {{projectName}} 构建失败', '# 🔴 {{projectName}} 构建失败\n\n版本: **{{version}}**\n分支: {{branch}}\n失败原因: {{errorMessage}}\n操作人: {{operator}}\n\n> 请及时处理', '{"projectName": "项目名称", "version": "版本号", "branch": "分支", "errorMessage": "错误信息", "operator": "操作人"}', 1),
('部署成功通知', 'WECOM', 'DEPLOY_SUCCESS', '[DRP] {{projectName}} 部署成功', '# {{projectName}} 部署成功\n\n版本: **{{version}}**\n环境: {{env}}\n操作人: {{operator}}\n部署时间: {{deployTime}}\n\n> 点击查看详情', '{"projectName": "项目名称", "version": "版本号", "env": "环境", "operator": "操作人", "deployTime": "部署时间"}', 1),
('部署失败告警', 'WECOM', 'DEPLOY_FAIL', '[DRP告警] {{projectName}} 部署失败', '# 🔴 {{projectName}} 部署失败\n\n版本: **{{version}}**\n环境: {{env}}\n失败原因: {{errorMessage}}\n操作人: {{operator}}\n\n> 请及时处理', '{"projectName": "项目名称", "version": "版本号", "env": "环境", "errorMessage": "错误信息", "operator": "操作人"}', 1),
('服务器离线告警', 'DINGTALK', 'SERVER_OFFLINE', '🚨 {{serverName}} 服务器离线', '## 🚨 服务器离线告警\n\n**服务器**: {{serverName}}\n**IP地址**: {{ip}}\n**告警类型**: {{alertType}}\n**发生时间**: {{time}}\n\n> 请及时检查服务器状态', '{"serverName": "服务器名称", "ip": "IP地址", "alertType": "告警类型", "time": "发生时间"}', 1);

-- 插入示例通知配置
INSERT INTO `ntf_notification_config` (`project_id`, `channel`, `event`, `enabled`, `config`, `recipients`, `rate_limit`) VALUES
(NULL, 'WECOM', 'BUILD_SUCCESS', 1, '{"webhookUrl": "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx", "safe": 0}', '@all', 60),
(NULL, 'WECOM', 'BUILD_FAIL', 1, '{"webhookUrl": "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx", "safe": 0}', '@all', 30),
(NULL, 'DINGTALK', 'BUILD_FAIL', 1, '{"webhookUrl": "https://oapi.dingtalk.com/robot/send?access_token=xxx"}', '群通知', 60),
(NULL, 'WECOM', 'DEPLOY_SUCCESS', 1, '{"webhookUrl": "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx", "safe": 0}', '@nick', 0),
(NULL, 'WECOM', 'DEPLOY_FAIL', 1, '{"webhookUrl": "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx", "safe": 0}', '@all', 30);

-- =====================================================
-- 事件类型说明
-- =====================================================
-- 构建事件: BUILD_START, BUILD_SUCCESS, BUILD_FAIL
-- 部署事件: DEPLOY_START, DEPLOY_SUCCESS, DEPLOY_FAIL, DEPLOY_APPROVAL
-- 回滚事件: ROLLBACK_SUCCESS, ROLLBACK_FAIL
-- 服务器事件: SERVER_OFFLINE, SERVER_ALERT
-- 安全事件: LOGIN_FAILED, PERMISSION_DENIED
