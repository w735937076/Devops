package com.drp.user.api.feign;

import com.drp.user.api.dto.SimpleUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign Client
 *
 * 配置说明：
 * - 单体部署：在 application.yml 中设置 spring.cloud.openfeign.client.config.drp-auth.url
 * - 微服务部署：通过注册中心发现服务，移除 url 配置即可
 */
@FeignClient(name = "drp-auth", path = "/api/users")
public interface UserFeignClient {

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    SimpleUserDTO getUserById(@PathVariable("id") Long id);
}