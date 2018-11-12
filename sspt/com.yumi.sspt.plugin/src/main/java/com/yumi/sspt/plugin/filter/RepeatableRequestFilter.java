/**
 * Copyright(c) Foresee Science & Technology Ltd.
 */
package com.yumi.sspt.plugin.filter;


import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * <pre>
 * 。
 * </pre>
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 * @date 2017年04月19日
 */
@Order(Ordered.HIGHEST_PRECEDENCE+FilterOrder.REPEATABLE)
public class RepeatableRequestFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 留空
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if((httpServletRequest.getRequestURI().indexOf("/druid/")!=-1)){
            chain.doFilter(request, response);
        }else{
            RepeatableHttpServletRequestWrapper requestWrapper =
                    new RepeatableHttpServletRequestWrapper(httpServletRequest).init();
            chain.doFilter(requestWrapper, response);
        }



    }



    @Override
    public void destroy() {
        // 留空
    }

    private static class RepeatableHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private static final Logger logger = LoggerFactory.getLogger(RepeatableHttpServletRequestWrapper.class);

        private byte[] byteIn;

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request
         * @throws IllegalArgumentException if the request is null
         */
        RepeatableHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(this.getInputStream()));
        }

        RepeatableHttpServletRequestWrapper init() throws IOException {
            if (byteIn == null) {
                byteIn = ByteStreams.toByteArray(super.getInputStream());
            }
            return this;
        }

        @Override
        public String getContextPath() {
            return "";
        }

        public ServletInputStream getInputStream() throws IOException {
            InputStream ins = new ByteArrayInputStream(byteIn);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return true;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {

                }

                @Override
                public int read() throws IOException {
                    return ins.read();
                }
            };
        }
    }
}
