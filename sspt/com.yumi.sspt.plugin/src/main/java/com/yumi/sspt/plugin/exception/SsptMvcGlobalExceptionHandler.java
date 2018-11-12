package com.yumi.sspt.plugin.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.yumi.sspt.plugin.rest.RestResponse;
import com.yumi.sspt.plugin.utils.ExceptionUtil;
import com.yumi.sspt.plugin.utils.Jackson;
import com.yumi.sspt.plugin.utils.RequestUtils;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@ControllerAdvice
public class SsptMvcGlobalExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.mvc.throw-exception-if-no-handler-found:true}")
    private Boolean throwException;

    @Autowired
    private DispatcherServlet dispatcherServlet;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private List<String> allHandlerUris;

    @PostConstruct
    public void init() {
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(throwException);

        initHandlerUris();
    }

    private void initHandlerUris() {
        Set<RequestMappingInfo> requestMappingInfos = handlerMapping.getHandlerMethods().keySet();

        allHandlerUris = new ArrayList<>();
        requestMappingInfos.stream()
                .forEach(requestMappingInfo -> {
                    Set<String> patterns = requestMappingInfo.getPatternsCondition().getPatterns();
                    for (String pattern : patterns) {
                        allHandlerUris.add(pattern);
                    }
                });
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public RestResponse handleException(HttpServletRequest request,
                                        Exception throwable) throws IOException {

        if (throwable.getMessage() == null) {
            return new RestResponse();
        }
        String traces = ExceptionUtil.getSimpleStackTrace(throwable);
        String rawBody = RequestUtils.getRawBody(request);
        logger.error("Handle Error, {}\nRequest:{}", traces, rawBody);

        RestResponse restResponse = handle(request, throwable);
        logger.info("Request end, URL: {}, result: {}",
                request.getRequestURL().toString(),
                Jackson.toJson(restResponse)
        );
        return restResponse;

    }

    private RestResponse handle(HttpServletRequest request, Exception throwable) throws IOException {
        if (throwable instanceof JsonMappingException) {
            return this.handleJsonMappingException(request, (JsonMappingException) throwable);
        }

        if (throwable instanceof NoHandlerFoundException) {
            return handle404(request);
        }

        String detailMessage = ExceptionUtil.getDetailMessage(throwable);
        return new RestResponse("0110119", String.format("出了点小问题,稍后再试! %s", detailMessage));
    }

    private RestResponse handleJsonMappingException(HttpServletRequest request,
                                                    JsonMappingException throwable) throws IOException {
        String errorMsg = "UNKNOWN";
        Iterator<JsonMappingException.Reference> iterator = throwable.getPath().iterator();
        if (iterator.hasNext()) {
            errorMsg = "字段:" + iterator.next().getFieldName() + "类型错误";
        }
        RestResponse<Object> errorResponse =
                RestResponse.error("0110119", errorMsg);
        return errorResponse;
    }

    private RestResponse handle404(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        int maxRatio = 0;
        int index = -1;
        for (int i = 0; i < allHandlerUris.size(); i++) {
            String handlerURI = allHandlerUris.get(i);
            int ratio = FuzzySearch.ratio(requestURI, handlerURI);
            if (ratio > maxRatio) {
                maxRatio = ratio;
                index = i;
            }
        }

        String fuzzyTip = "";
        if (index >= 0) {
            fuzzyTip = "你是否想请求:" + allHandlerUris.get(index);
        }
        return new RestResponse("404", String.format("uri:%s 不存在, %s", requestURI, fuzzyTip));
    }
}