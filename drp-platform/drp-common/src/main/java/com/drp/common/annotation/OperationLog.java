package com.drp.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * <p>
 * 标注在 Controller 方法上，用于自动记录操作日志
 *
 * @author Nick
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    String type() default "";

    /**
     * 操作描述
     */
    String description() default "";

    /**
     * 是否记录参数
     */
    boolean logParams() default true;

    /**
     * 是否记录返回值
     */
    boolean logResult() default false;

    /**
     * 敏感参数脱敏（参数名，多个用逗号分隔）
     */
    String sensitiveParams() default "";
}
