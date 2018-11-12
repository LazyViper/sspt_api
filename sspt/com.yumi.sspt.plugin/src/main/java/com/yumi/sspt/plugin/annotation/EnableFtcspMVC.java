package com.yumi.sspt.plugin.annotation;

import com.yumi.sspt.plugin.config.SsptAspectConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * <pre>
 * TODO
 * </pre>
 *
 * @author chenwenlong chenwenlong@foresee.com.cn
 * @version 1.00.00
 * @date 2017年08月01日
 * <p>
 * <pre>
 * 修改记录
 * 修改后版本: 修改人： 修改日期: 修改内容:
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({SsptAspectConfiguration.class})
@AutoConfigureAfter(DispatcherServletAutoConfiguration.class)
public @interface EnableFtcspMVC {
    Class[] threadDefines() default  {};
}
