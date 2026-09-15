package com.qingke.common;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 使用此注解的方法会自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLogAnnotation {
    
    /**
     * 操作模块
     */
    String module();
    
    /**
     * 操作类型：login, logout, add, update, delete, audit, query
     */
    String type();
    
    /**
     * 操作描述
     */
    String description() default "";
}
