/*
* Copyright（c） Foresee Science & Technology Ltd.
*/

package com.yumi.sspt.plugin.config;


import com.yumi.sspt.plugin.aspect.SsptRestInterceptor;
import com.yumi.sspt.plugin.filter.AsyncFilter;
import com.yumi.sspt.plugin.filter.RepeatableRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;


/**
 * <pre>
 * FtcspMVC用到的组件, 在这里生成
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
public class SsptAspectConfiguration {

    @Autowired
    private ConfigurableEnvironment env;


    @Bean
    public RepeatableRequestFilter repeatableRequestFilter() {
        return new RepeatableRequestFilter();
    }


    @Bean
    @ConditionalOnClass(name = "org.eclipse.jetty.util.Jetty")
    public AsyncFilter asyncFilter() {
        return new AsyncFilter();
    }

    @Bean
    public SsptRestInterceptor ftcspRestInterceptor() {
        return new SsptRestInterceptor();
    }

}
