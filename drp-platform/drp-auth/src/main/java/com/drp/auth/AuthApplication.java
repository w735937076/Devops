package com.drp.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DRP 认证服务启动类
 *
 * @author Nick
 */
@SpringBootApplication(scanBasePackages = "com.drp")
@MapperScan("com.drp.auth.repository")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
