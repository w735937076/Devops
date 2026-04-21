-- 修改 prj_project 表的逻辑删除字段名 is_deleted -> deleted
ALTER TABLE `prj_project` CHANGE COLUMN `is_deleted` `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除';