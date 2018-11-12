/*
* Copyright（c） Foresee Science & Technology Ltd.
*/

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
 * @date 2017年09月15日
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadGroup {

    /**
     * 所使用的线程组名称
     */
    String value() default DefaultExecutors.DEFAULT;
}
