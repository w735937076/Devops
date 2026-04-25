package com.drp.notify;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.drp")
@MapperScan("com.drp.notify.repository")
@EnableAsync
public class NotifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifyApplication.class, args);
    }
}
