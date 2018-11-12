/*
 * Copyright（c） Foresee Science & Technology Ltd.
 */

package com.yumi.sspt.plugin.utils;

/**
 * <pre>
 * TODO
 * </pre>
 *
 * @author chenwenlong chenwenlong@foresee.com.cn
 * @version 1.00.00
 * @date 2017年07月28日
 *
 * <pre>
 * 修改记录
 * 修改后版本: 修改人： 修改日期: 修改内容:
 * </pre>
 */

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Set;

/**
 * <pre>
 * ExceptionUtil
 * </pre>
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 * @date 2017年05月11日
 */
public class ExceptionUtil {

    public static String getSimpleStackTrace(Thread thread) {
        StackTraceElement[] stackTraces = thread.getStackTrace();
        Set<String> traceLines = Sets.newLinkedHashSetWithExpectedSize(10);
        traceLines.add(thread.toString());
        for (StackTraceElement stackTrace : stackTraces) {
            String line = stackTrace.toString();
            filter(traceLines, line);
        }
        return StringUtils.join(traceLines, "\n");
    }

    public static String getSimpleStackTrace(Throwable throwable) {
        String wholeStackTraceString = ExceptionUtils.getStackTrace(throwable);
        String[] lines = StringUtils.split(wholeStackTraceString, "\n");
        Set<String> traceLines = Sets.newLinkedHashSetWithExpectedSize(10);
        for (String line : lines) {
            filter(traceLines, line);
        }
        return StringUtils.join(traceLines, "\n");
    }

    private static void filter(Set<String> traceLines, String line) {
        if (line.startsWith("com.foresee")) {
            traceLines.add("\tat " + line);
        }
    }

    public static String getDetailMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null) {
            return "";
        }
        String message = throwable.getMessage();
        StringBuilder builder = new StringBuilder(message);

        Throwable inner = throwable.getCause();
        while (inner != null) {
            builder.append("<=>")
                    .append(inner.getMessage());
            inner = inner.getCause();
        }

        return builder.toString();
    }
}
