package com.drp.project.client;

import com.drp.user.api.dto.SimpleUserDTO;
import com.drp.user.api.service.UserServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户服务客户端
 *
 * 单体架构：直接调用本地 UserServiceInterface 实现
 * 微服务架构：可替换为 Feign 调用（通过 profile 切换）
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    @Autowired
    private UserServiceInterface userService;

    /**
     * 获取用户信息
     */
    public UserInfo getUserById(Long userId) {
        try {
            SimpleUserDTO user = userService.getUserById(userId);
            if (user != null) {
                UserInfo userInfo = new UserInfo();
                userInfo.setId(user.getId());
                userInfo.setUsername(user.getUsername());
                userInfo.setRealName(user.getRealName());
                return userInfo;
            }
        } catch (Exception e) {
            log.error("获取用户信息失败 | userId: {} | error: {}", userId, e.getMessage());
        }

        // 返回默认信息
        UserInfo defaultInfo = new UserInfo();
        defaultInfo.setId(userId);
        defaultInfo.setUsername("user_" + userId);
        defaultInfo.setRealName("用户 " + userId);
        return defaultInfo;
    }

    /**
     * 用户信息 DTO
     */
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
    }
}