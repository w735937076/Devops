-- =====================================================
-- 构建模块 SQL 更新
-- 2026-04-25 更新
-- =====================================================

-- =====================================================
-- 1. 确认 bld_artifact 表结构
-- 确保包含所有必要字段支持产物下载功能
-- =====================================================
-- 注意：如果 bld_artifact 表已存在且没有 create_time 字段，执行以下 ALTER
-- 如果表是新创建的（init-build.sql），则已包含所有字段

-- ALTER TABLE `bld_artifact`
-- ADD COLUMN IF NOT EXISTS `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `checksum`;

-- =====================================================
-- 2. 产物文件存储目录说明
-- =====================================================
-- Windows: D:\DrpArtifact\{projectId}\{buildId}\
-- Linux:   /tmp/drp-builds/{projectId}/{buildId}/
--
-- 示例结构:
-- D:\DrpArtifact\
-- ├── 1\
-- │   ├── 1\
-- │   │   ├── source-code\
-- │   │   ├── target\
-- │   │   │   ├── app-1.0.0.jar
-- │   │   │   └── app-1.0.0.jar.original
-- │   │   └── ...
-- │   └── 2\
-- │       └── ...

-- =====================================================
-- 3. 产物下载 URL 格式
-- =====================================================
-- GET /api/builds/{buildId}/artifact/download?path={encoded_path}
--
-- path 参数为 URL 编码后的文件绝对路径
-- 例如: /api/builds/1/artifact/download?path=D%3A%2FDrpArtifact%2F1%2F1%2Ftarget%2Fapp.jar

-- =====================================================
-- 4. 构建产物 JSON 格式 (存储在 bld_build_record.artifacts)
-- =====================================================
-- [
--   {
--     "name": "app.jar",
--     "path": "D:/DrpArtifact/1/1/target/app.jar",
--     "size": 12345678,
--     "downloadUrl": "/api/builds/1/artifact/download?path=D%3A%2FDrpArtifact%2F1%2F1%2Ftarget%2Fapp.jar",
--     "createTime": "2026-04-25 10:30:00"
--   }
-- ]

-- =====================================================
-- 5. 构建详情版本号格式 (前端展示用，非存储)
-- =====================================================
-- 格式: v{date}-{commitId(前7位)}-{buildNumber}
-- 示例: v20260425-a1b2c3d-001

-- =====================================================
-- 6. 流水线阶段 JSON 格式 (来自 bld_pipeline.stages)
-- =====================================================
-- [
--   {"name": "checkout", "type": "GIT_CLONE", "enabled": true},
--   {"name": "compile", "type": "MAVEN_BUILD", "enabled": true, "config": {"goals": "compile"}},
--   {"name": "test", "type": "MAVEN_BUILD", "enabled": true, "config": {"goals": "test"}},
--   {"name": "package", "type": "MAVEN_BUILD", "enabled": true, "config": {"goals": "package -DskipTests"}}
-- ]

-- =====================================================
-- 7. 构建参数 JSON 格式 (来自 bld_pipeline.build_params)
-- =====================================================
-- {"jdk": "17", "maven": "3.8.6", "node": "18.x", "npm": "9.x"}
