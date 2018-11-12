package com.yumi.sspt.plugin.isolation;

/**
 * <pre>
 *  业务线程组定义, 默认都会有的线程池
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
public interface DefaultExecutors {

    /**
     * 业务默认线程池
     */
    @ExecutorCfg
    String DEFAULT = "DEF-BIZ";

    /**
     * 业务核心线程池
     */
    @ExecutorCfg(coreSize = 10, maxSize = 100, timeout = "${core.servlet.timeout:5}")
    String CORE = "CORE-BIZ";
}
