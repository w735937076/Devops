package com.drp.user.api.service;

import com.drp.user.api.dto.SimpleUserDTO;

/**
 * 用户服务接口（定义在 drp-user-api，供跨模块调用）
 *
 * 使用方式：
 * - 单体架构：由 drp-boot 配置为直接注入本地实现
 * - 微服务架构：通过 Feign 远程调用
 */
public interface UserServiceInterface {

    /**
     * 根据ID获取用户信息（简化版）
     */
    SimpleUserDTO getUserById(Long id);
}