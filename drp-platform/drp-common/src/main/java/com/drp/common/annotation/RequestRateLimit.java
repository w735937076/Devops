package com.drp.common.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * <p>
 * 标注在 Controller 方法上，用于接口限流
 *
 * @author Nick
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestRateLimit {

    /**
     * 限流key
     */
    String key() default "";

    /**
     * 限流时间窗口（秒）
     */
    int timeWindow() default 60;

    /**
     * 最大请求次数
     */
    int maxRequests() default 100;

    /**
     * 提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
