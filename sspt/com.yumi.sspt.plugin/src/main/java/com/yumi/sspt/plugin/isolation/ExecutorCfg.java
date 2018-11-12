package com.yumi.sspt.plugin.isolation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 *
 * </pre>
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.00.00
 * @date 2017年09月17日
 * <p>
 * <pre>
 * 修改记录
 * 修改后版本: 修改人： 修改日期: 修改内容:
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutorCfg {

    int coreSize() default 4;
    int maxSize() default 200;

    /**
     * 线程空闲存活时间,
     */
    int keepAliveTime() default 10*60*1000; // 10分钟

    /**
     * 等待队列的大小
     * 所有线程都已经在忙的时候, 新进来的请求会进入等待队列
     */
    int capacity() default 50;

    /**
     * 超时时间
     * @return
     */
    String timeout() default "${async.servlet.timeout:90}";
}
