package com.drp.build.service;

import com.drp.build.dto.BuildDetailDTO;
import com.drp.build.dto.BuildTriggerRequest;
import com.drp.build.entity.BuildRecord;
import com.drp.common.dto.PageResponse;
import com.drp.project.entity.Project;
import org.springframework.web.socket.WebSocketSession;

public interface BuildService {

    /**
     * 触发构建
     */
    BuildDetailDTO triggerBuild(BuildTriggerRequest request, String triggerUser);

    /**
     * 分页查询构建记录
     */
    PageResponse<BuildDetailDTO> queryPage(Long projectId, Integer status, String branch, int page, int size);

    /**
     * 获取构建详情
     */
    BuildDetailDTO getBuildDetail(Long id);

    /**
     * 取消构建
     */
    void cancelBuild(Long id);

    /**
     * 获取构建日志
     */
    String getBuildLog(Long id);

    /**
     * 推送构建日志到 WebSocket
     */
    void pushBuildLog(Long buildId, String logContent);

    /**
     * 标记构建结束
     */
    void finishBuild(Long buildId, int status, String errorMessage);

    /**
     * 标记构建结束（带产物）
     */
    void finishBuild(Long buildId, int status, String errorMessage, String artifacts);

    /**
     * 注册 WebSocket 会话
     */
    void registerLogSession(Long buildId, WebSocketSession session);

    /**
     * 注销 WebSocket 会话
     */
    void unregisterLogSession(Long buildId);

    /**
     * 生成构建产物信息
     */
    String generateArtifacts(Project project, BuildRecord record, String workspace);
}
