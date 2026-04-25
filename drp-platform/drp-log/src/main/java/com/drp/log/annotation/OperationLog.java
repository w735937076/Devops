package com.drp.log.annotation;

import com.drp.log.enums.OperationType;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    String value();

    OperationType type();

    String projectParam() default "";

    String envParam() default "";

    String versionParam() default "";
}
