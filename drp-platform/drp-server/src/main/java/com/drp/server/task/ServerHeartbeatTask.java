package com.drp.server.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drp.server.entity.Server;
import com.drp.server.enums.ServerStatus;
import com.drp.server.repository.ServerRepository;
import com.drp.server.service.SshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务器心跳检测定时任务
 *
 * <p>功能说明：
 * <ul>
 *   <li>每隔60秒执行一次服务器在线状态检测</li>
 *   <li>仅检测当前状态为"在线"的服务器</li>
 *   <li>通过SSH连接测试服务器是否可达</li>
 *   <li>根据检测结果更新服务器状态和最后心跳时间</li>
 * </ul>
 *
 * <p>状态说明：
 * <ul>
 *   <li>ONLINE(1) - 在线</li>
 *   <li>OFFLINE(0) - 离线</li>
 *   <li>BUSY(2) - 忙碌（暂时未使用）</li>
 * </ul>
 *
 * @author Nick
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServerHeartbeatTask {

    private final ServerRepository serverRepository;
    private final SshService sshService;

    /**
     * 定时检测服务器心跳
     *
     * <p>执行频率：每60秒执行一次（fixedRate = 60000）
     *
     * <p>处理流程：
     * <ol>
     *   <li>查询所有状态为"在线"的服务器</li>
     *   <li>遍历每个服务器执行SSH连接测试</li>
     *   <li>连接成功：更新最后心跳时间为当前时间，保持在线状态</li>
     *   <li>连接失败：更新服务器状态为"离线"</li>
     * </ol>
     */
    @Scheduled(fixedRate = 60000)
    public void checkHeartbeat() {
        // 查询所有在线的服务器
        List<Server> servers = serverRepository.selectList(
                new LambdaQueryWrapper<Server>().eq(Server::getStatus, ServerStatus.ONLINE.getValue())
        );

        log.debug("开始心跳检测，待检测服务器数量：{}", servers.size());

        for (Server server : servers) {
            try {
                // 执行SSH连接测试
                sshService.testConnection(server);

                // 连接成功：更新心跳时间，保持在线状态
                server.setLastHeartbeat(LocalDateTime.now());
                server.setStatus(ServerStatus.ONLINE.getValue());

                log.debug("服务器 {} 心跳检测成功", server.getHostname());
            } catch (Exception e) {
                // 连接失败：标记为离线
                log.warn("服务器 {} 心跳检测失败: {}", server.getHostname(), e.getMessage());
                server.setStatus(ServerStatus.OFFLINE.getValue());
            }

            // 更新服务器状态
            serverRepository.updateById(server);
        }

        log.debug("心跳检测完成");
    }
}
