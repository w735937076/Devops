package com.drp.build;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.drp")
@EnableAsync
public class BuildApplication {
    public static void main(String[] args) {
        SpringApplication.run(BuildApplication.class, args);
    }
}
