package com.drp.boot.config;

import com.drp.auth.dto.UserDTO;
import com.drp.auth.service.UserService;
import com.drp.user.api.dto.SimpleUserDTO;
import com.drp.user.api.service.UserServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 本地用户服务客户端（单体架构专用）
 *
 * 在单体架构下，直接调用同一进程内的 UserService
 * 不需要通过 HTTP/Feign 调用，避免上下文丢失问题
 */
@Configuration
public class LocalUserServiceClient {

    @Autowired
    private UserService userService;

    @Bean
    @Primary
    public UserServiceInterface userServiceInterface() {
        return new UserServiceInterface() {
            @Override
            public SimpleUserDTO getUserById(Long id) {
                UserDTO user = userService.getById(id);
                if (user == null) {
                    return null;
                }
                SimpleUserDTO dto = new SimpleUserDTO();
                dto.setId(user.getId());
                dto.setUsername(user.getUsername());
                dto.setRealName(user.getRealName());
                return dto;
            }
        };
    }
}