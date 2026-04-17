package com.drp.common.annotation;

import java.lang.annotation.*;

/**
 * 重复提交校验注解
 * <p>
 * 标注在 Controller 方法上，用于防止重复提交
 *
 * @author Nick
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoRepeatSubmit {

    /**
     * 锁key的前缀
     */
    String prefix() default "";

    /**
     * 锁的过期时间（毫秒）
     * <p>
     * 默认5秒
     */
    long expireMillis() default 5000;

    /**
     * 提示信息
     */
    String message() default "请勿重复提交";
}
