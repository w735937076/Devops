-- 部署权限增量脚本
INSERT INTO `sys_permission` (`code`, `name`, `description`, `type`, `sort`, `status`)
SELECT 'DEPLOY_APPROVE', '审批部署', '审批生产部署与高风险发布', 'API', 24, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_permission` WHERE `code` = 'DEPLOY_APPROVE');

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p
WHERE p.code = 'DEPLOY_APPROVE'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp WHERE rp.role_id = 1 AND rp.permission_id = p.id
  );
