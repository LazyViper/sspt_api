package com.yumi.sspt.plugin.utils;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;

/**
 * <pre>
 * 请求工具类
 * </pre>
 *
 * @author chenwenlong chenwenlong@foresee.com.cn
 * @version 1.00.00
 * @date 2017年06月30日
 * <p>
 * <pre>
 * 修改记录
 * 修改后版本: 修改人： 修改日期: 修改内容:
 * </pre>
 */
public class RequestUtils {

    /**
     * 获取此次请求的HTTP报文
     * @param request ServletRequest对象
     * @return http报文
     */
    public static String getRawBody(HttpServletRequest request){
        String query = StringUtils.defaultIfBlank(request.getQueryString(), "");
        query = "".equals(query) ? query : "?"+query;
        StringBuilder rawBody =
                new StringBuilder(request.getMethod()
                        + " " + request.getRequestURI() + query
                        + " " + request.getProtocol()
                        + "\n");

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            rawBody.append(headerName+": "+headerValue+"\n");
        }
        rawBody.append("\n");
        String requestBody;
        try {
            InputStreamReader reader = new InputStreamReader(request.getInputStream(), Charsets.UTF_8);
            requestBody = CharStreams.toString(reader);
            rawBody.append(requestBody);
        } catch (IOException e) {
            rawBody.append("Impossible:").append(e.getMessage());
        }
        rawBody.append("\nIP:").append(request.getRemoteAddr());
        return rawBody.toString();
    }

    public static HttpServletRequest getRequest() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }
        throw new UnsupportedOperationException("没有HttpServletRequest");
    }
}