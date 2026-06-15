package com.estudy.common.annotation.permission;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 标记在方法或类上，表示需要特定权限才能访问
 */
@Target({ElementType.METHOD}) // 可用于方法和类
@Retention(RetentionPolicy.RUNTIME) // 运行时保留，AOP才能读取
@Documented
public @interface RequirePermission {
    /**
     * 需要的权限标识，支持多个（满足其一即可 或 全部满足，取决于业务）
     */
    String value() default "";

}
