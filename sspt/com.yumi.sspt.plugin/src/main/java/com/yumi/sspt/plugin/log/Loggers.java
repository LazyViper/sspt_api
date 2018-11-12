package com.yumi.sspt.plugin.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * Logger生成
 * </pre>
 *
 * @author chenwenlong chenwenlong@foresee.com.cn
 * @version 1.00.00
 * @date 2017年06月26日
 * <p>
 * <pre>
 * 修改记录
 * 修改后版本: 修改人： 修改日期: 修改内容:
 * </pre>
 */
public abstract class Loggers {
    public static Logger make() {
        Throwable t = new Throwable();
        StackTraceElement directCaller = t.getStackTrace()[1];
        return LoggerFactory.getLogger(directCaller.getClassName());
    }
}