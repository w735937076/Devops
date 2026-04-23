package com.drp.build.service.executor;

import com.drp.build.entity.BuildRecord;
import com.drp.build.entity.Pipeline;
import com.drp.project.entity.Project;

/**
 * 构建执行器接口
 */
public interface BuildExecutor {

    /**
     * 获取支持的项目类型
     */
    String getSupportedType();

    /**
     * 异步执行构建
     */
    void executeAsync(BuildRecord record, Project project, Pipeline pipeline);

    /**
     * 同步执行构建 (用于测试)
     */
    BuildResult executeSync(BuildRecord record, Project project, Pipeline pipeline);

    /**
     * 构建结果
     */
    record BuildResult(boolean success, String output, String error, int exitCode) {}
}
