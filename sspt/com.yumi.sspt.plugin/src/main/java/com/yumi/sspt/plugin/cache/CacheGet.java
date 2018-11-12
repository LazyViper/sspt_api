package com.yumi.sspt.plugin.cache;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * CacheGet
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheGet {

    Class<?> cls() default EnclosingContainer.class;

    String key() default "";

    int timeout() default 1;

    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * 只是一个占位符,表示注解CacheGet所在的类
     */
    class EnclosingContainer {

    }

}
