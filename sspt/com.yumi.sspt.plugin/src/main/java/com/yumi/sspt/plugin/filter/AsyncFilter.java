package com.yumi.sspt.plugin.filter;


import com.google.common.base.Charsets;
import com.yumi.sspt.plugin.isolation.ExecutorFactory;
import com.yumi.sspt.plugin.rest.RestResponse;
import com.yumi.sspt.plugin.support.ThreadPool;
import com.yumi.sspt.plugin.utils.ExceptionUtil;
import com.yumi.sspt.plugin.utils.Jackson;
import com.yumi.sspt.plugin.utils.NetworkUtil;
import com.yumi.sspt.plugin.utils.RequestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.web.util.NestedServletException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <pre>
 * 线程池隔离的Filter
 * </pre>
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.00.00
 * @date 2017年08月13日
 * <p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE + FilterOrder.ASYNC)
public class AsyncFilter implements Filter {
    private final  Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ExecutorFactory executeFactory;

    private final static String START_TIME = AsyncFilter.class.getName() + ".START_TIME";

    // request属性, 执行当前这个request的线程
    private final static String EXECUTION_THREAD = AsyncFilter.class.getName() + ".EXECUTION_THREAD";

    @Override
    public void init(FilterConfig filterConfig) {
        // 空方法
    }

    private void dumpRequest(HttpServletRequest httpServletRequest, ThreadPool executor) {
        try {
            doDumpRequest(httpServletRequest, executor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void doDumpRequest(HttpServletRequest httpServletRequest, ThreadPool executor)
            throws IOException {
        String url = (httpServletRequest).getRequestURL().toString();
        String queryString=(httpServletRequest).getQueryString();
        if(StringUtils.isNotBlank(queryString)){
            url=String.format("%s?%s",url,queryString);
        }
        String body = IOUtils.toString(httpServletRequest.getInputStream(), Charsets.UTF_8.name());
        if (logger.isInfoEnabled()) {
            String ipAddress = NetworkUtil.getIpAddress(httpServletRequest);
            logger.info("request begin, remote:{}, pool:{}, url:{}\n{}", ipAddress, executor, url, body);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if((httpServletRequest.getRequestURI().indexOf("/druid/")!=-1)){
            chain.doFilter(request, servletResponse);
        }else{
            ServletResponse response = wrapResponse(servletResponse);

            ThreadPool executor = executeFactory.getExecutor(httpServletRequest);

            startAsync(request, executor.getTimeout());
            String requestId = getRequestId(request);
            MDC.put("requestId", requestId);

            long jettyThreadId = Thread.currentThread().getId();

            executor.execute(() -> {
                request.setAttribute(START_TIME, System.currentTimeMillis());

                SimpleRequestContextHolder.initRequest((HttpServletRequest)request);

                try {
                    MDC.put("requestId", requestId);
                    long bizThreadId = Thread.currentThread().getId();

                    if (bizThreadId == jettyThreadId) {
                        // 线程池已经用光
                        handleBusy(request, response, executor);
                    } else {
                        // FIXME 当线程池使用超过4/5, 打印正在运行线程
                        // (遍历所有线程, 跟当前线程池的前缀比较)
                        request.setAttribute(EXECUTION_THREAD, Thread.currentThread());
                        dumpRequest(httpServletRequest, executor);
                        chain.doFilter(request, response);
                    }
                } catch (NestedServletException e) {
                    if (e.getCause() != null && e.getCause().getClass().equals(NullPointerException.class)) {
                        logger.warn("已经调用complete, {}", e.getMessage());
                        return;
                    }
                    handleException(request, response, e);
                } catch (Exception e) {
                    handleException(request, response, e);
                } finally {
                    try {
                        dumpResponse(request, response);
                        request.getAsyncContext().complete();
                    } catch (Exception e) {
                        logger.warn("已经调用complete, {}", e.getMessage());
                    }
                }
            });
        }

    }

    private ServletResponse wrapResponse(ServletResponse response) {
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        ByteArrayPrintWriter pw = new ByteArrayPrintWriter();
        response = new ByteArrayHttpResponse(httpServletResponse, pw);
        return response;
    }

    private void dumpResponse(ServletRequest request, ServletResponse response)
            throws IOException {
        if (response instanceof ByteArrayHttpResponse) {
            ByteArrayHttpResponse byteArrayHttpResponse = (ByteArrayHttpResponse) response;
            byte[] bytes = byteArrayHttpResponse.toByteArray();
            String body = new String(bytes, Charsets.UTF_8);
            byteArrayHttpResponse.getResponse().getOutputStream().write(bytes);
            Long startTime = (Long) request.getAttribute(START_TIME);
            if (startTime != null) {
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;


                HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                String url = httpServletRequest.getRequestURL() + "?" + httpServletRequest.getQueryString();
                logger.info("Request end({}), URL: {}, result: {}",
                        elapsedTime, url, body);
            } else {
                logger.info("startTime is null");
            }
        }
    }

    private void startAsync(ServletRequest request, int asyncTimeout) {
        // 异步超时时间
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(asyncTimeout);

        asyncContext.addListener(new AsyncListener() {

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                ServletRequest suppliedRequest = event.getSuppliedRequest();

                logStack(suppliedRequest);
                ServletResponse servletResponse = event.getSuppliedResponse();
                handleTimeout(suppliedRequest, servletResponse);
                event.getAsyncContext().complete();
            }

            private void logStack(ServletRequest suppliedRequest) {
                Thread executionThread = (Thread) suppliedRequest.getAttribute(EXECUTION_THREAD);
                String simpleStackTrace = ExceptionUtil.getSimpleStackTrace(executionThread);
                HttpServletRequest httpServletRequest = (HttpServletRequest) suppliedRequest;
                String url = httpServletRequest.getRequestURL() + "?" + httpServletRequest.getQueryString();

                logger.error("已经运行{}ms, url:{}, stacktrace:\n{}", asyncTimeout, url, simpleStackTrace);
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                ServletResponse servletResponse = event.getSuppliedResponse();
                ServletRequest servletRequest = event.getSuppliedRequest();
                Throwable throwable = event.getThrowable();
                handleException(servletRequest, servletResponse, throwable);
                event.getAsyncContext().complete();
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {

            }
        });

    }

    private void handleTimeout(ServletRequest request, ServletResponse response) {
        String requestId = getRequestId(request);
        MDC.put("requestId", requestId);
        String errorMsg = "系统访问超时,请稍后再试!";
        logger.error(errorMsg);
        RestResponse<Object> body = new RestResponse("0110122", errorMsg);
        writeJsonToClient(request, (HttpServletResponse) response, body);
    }

    private void handleException(ServletRequest request, ServletResponse response, Throwable e) {
        String rawBody = RequestUtils.getRawBody((HttpServletRequest) request);
        logger.error("运行出错, {}", rawBody, e);
        String detailMsg = e == null ? "" : ExceptionUtil.getDetailMessage(e);
        String errorMsg = String.format("系统出现了一点小问题,请稍后再试! 详情:%s", detailMsg);
        RestResponse<Object> body = new RestResponse("0110121", errorMsg);
        writeJsonToClient(request, (HttpServletResponse) response, body);
    }

    private void writeJsonToClient(ServletRequest request, HttpServletResponse httpResponse, RestResponse result) {
        String body = Jackson.toJson(result);

        httpResponse.setCharacterEncoding("utf-8");
        httpResponse.setHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        try {
            httpResponse.getOutputStream().write(body.getBytes(Charsets.UTF_8));
        } catch (IOException e) {
            logger.error("写入到客户端失败", e);
        }
    }

    private String getRequestId(ServletRequest request) {
        String requestId = request.getParameter("requestId");
        if (StringUtils.isBlank(requestId)) {
            requestId = "";
        }
        return requestId;
    }

    private void handleBusy(ServletRequest request, ServletResponse response, ThreadPool executor) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String url = httpServletRequest.getRequestURL() + "?" + httpServletRequest.getQueryString();

        logger.error("系统繁忙,请稍后再试, url:{}, pool:{}", url, executor);
        RestResponse busy = RestResponse.error("0110120", "系统繁忙,请稍后再试!");
        writeJsonToClient(request, (HttpServletResponse) response, busy);
    }

    @Override
    public void destroy() {
        SimpleRequestContextHolder.resetRequestAttributes();
    }
}
