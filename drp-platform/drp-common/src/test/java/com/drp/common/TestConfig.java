package com.drp.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 测试配置类
 *
 * @author Nick
 */
@TestConfiguration
public class TestConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        // 提供测试用的 RedisTemplate mock
        return new org.springframework.data.redis.core.StringRedisTemplate();
    }
}
