package com.yumi.sspt.plugin.filter;


import org.springframework.core.NamedThreadLocal;

import javax.servlet.http.HttpServletRequest;

/**
 * RequestContainer
 *
 * @author liyuming@foresee.com.cn
 * @version 1.0
 * @time 2018/10/31 0031
 */
public class SimpleRequestContextHolder {


    private static final ThreadLocal<HttpServletRequest>  requestHolder     = new NamedThreadLocal<HttpServletRequest>(
            "Simple Request attributes");



    public static void initRequest(HttpServletRequest request) {
        requestHolder.remove();
        requestHolder.set(request);
    }

    public static HttpServletRequest getRequest() {
        HttpServletRequest request = requestHolder.get();
        return request;
    }

    public static boolean existsRequest() {
        return requestHolder.get() != null;
    }

    /**
     * Reset the RequestAttributes for the current thread.
     */
    public static void resetRequestAttributes() {
        requestHolder.remove();
    }
}
