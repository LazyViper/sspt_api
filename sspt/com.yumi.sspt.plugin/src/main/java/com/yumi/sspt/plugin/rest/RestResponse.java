/**
 * Copyright(c) Foresee Science & Technology Ltd.
 */
package com.yumi.sspt.plugin.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.yumi.sspt.plugin.response.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * RestController方法返回结果封装类
 * </pre>
 *
 * @author chenqiang@foresee.com.cn
 * @date 2016年12月1日
 * @version 1.00.00
 */

@JsonInclude(Include.NON_NULL)
public class RestResponse<T> implements Serializable {

    /**
     * 成功代码：0
     */
    private static final String CODE_SUCCESS     = "0";

    /**
     * serialVersionUID:TODO。
     */
    private static final long   serialVersionUID = 4572910611616655490L;

    /**
     * 头信息
     */
    private RestResponseHead    head;

    /**
     * 业务数据
     */
    private T                   body;

    /**
     * 扩展信息
     */
    private Map<String, Object> extMap;
    
    /**
     * 错误信息格式化参数
     */
    @JsonIgnore
    private Object[] errorParams;

    /**
     * 根据数据创建接口调用成功的响应对象
     * @param data 数据
     * @return RestResponse<T> 响应对象
     */
    public static <T> RestResponse<T> successData(T data) {
        return new RestResponse<T>(data);
    }

    /**
     * 根据消息创建接口调用成功的响应对象
     * @param message 成功的消息
     * @return RestResponse<T> 响应对象
     */
    public static <T> RestResponse<T> successMessage(String message) {
        return new RestResponse<T>(CODE_SUCCESS, message);
    }

    /**
     * 根据消息及数据创建接口调用成功的响应对象
     * @param message 成功的消息
     * @param data 数据
     * @return RestResponse<T> 响应对象
     */
    public static <T> RestResponse<T> success(String message, T data) {
        return new RestResponse<T>(message, data);
    }

    /**
     * 根据错误代码及错误信息、错误信息格式化参数创建接口调用失败的响应对象
     * @param errorCode 错误代码
     * @param errorParams 错误信息格式化参数
     * @return RestResponse<T> 响应对象
     */
    public static <T> RestResponse<T> error(String errorCode, Object... errorParams) {
        RestResponse<T> response = new RestResponse<T>(errorCode, errorParams[0].toString());
        response.setErrorParams(errorParams);
        return response;
    }
    
    /**
     * 根据错误代码及错误信息、错误信息格式化参数创建接口调用失败的响应对象
     * @return RestResponse<T> 响应对象
     */
    public static RestResponse<String> redirect(String url) {
        RestResponse<String> response = new RestResponse<String>(null, RestResponseHead.REDIRECT_CODE, null, url);
        return response;
    }

    public RestResponse() {

    }

    public RestResponse(String errorCode, String errorMsg) {
        this(null, errorCode, errorMsg, null, 0);
    }

    public RestResponse(T body) {
        this(null, CODE_SUCCESS, null, body, 0);
    }

    public RestResponse(String errorMsg, T body) {
        this(null, CODE_SUCCESS, errorMsg, body, 0);
    }

    public RestResponse(String errorCode, String errorMsg, T body) {
        this(null, errorCode, errorMsg, body, 0);
    }

    public RestResponse(String requestId, String errorCode, String errorMsg) {
        this(requestId, errorCode, errorMsg, null, 0);
    }

    public RestResponse(String requestId, String errorCode, String errorMsg, T body) {
        this(requestId, errorCode, errorMsg, body, 0);
    }

    public RestResponse(String requestId, String errorCode, String errorMsg, T body, long time) {
        this(requestId, errorCode, null, errorMsg, body, time);
    }

    public RestResponse(String requestId, String errorCode, String errorNo, String errorMsg, T body, long time) {
        this.head = new RestResponseHead(requestId, errorCode, errorMsg, System.currentTimeMillis(), time);
        this.head.setErrorNo(errorNo);
        this.body = body;
    }

    public RestResponse(RestResponseHead head, T body) {
        this.head = head;
        this.body = body;
    }

    public RestResponseHead getHead() {
        return head;
    }

    public void setHead(RestResponseHead head) {
        this.head = head;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.head.isSuccess();
    }

    @JsonIgnore
    public boolean isError() {
        return this.head.isError();
    }

    @SuppressWarnings("rawtypes")
    @JsonIgnore
    public Object getData() {
        if (body != null && body instanceof Map) {
            if (((Map) body).get("pager") != null) {
                return ((Map) body).get("data");
            }
        }

        return body;
    }

    public void putExtObject(String key, Object obj) {
        if (extMap == null) {
            extMap = new HashMap<String, Object>();
        }

        extMap.put(key, obj);
    }

    public Object getExtObject(String key) {
        if (extMap == null) {
            return null;
        }

        return extMap.get(key);
    }

    public void removeExtObject(String key) {
        if (extMap == null) {
            return;
        }

        extMap.remove(key);
    }

    public Map<String, Object> getExtMap() {
        return extMap;
    }

    public void setExtMap(Map<String, Object> extMap) {
        this.extMap = extMap;
    }
    
    public Object[] getErrorParams() {
        return errorParams;
    }
    
    public void setErrorParams(Object[] errorParams) {
        this.errorParams = errorParams;
    }

    public static RestResponse<StringResponse> create(String key, String value) {
        return create(key, value, StringResponse.class);
    }

    public static RestResponse<IntegerResponse> create(String key, Integer value) {
        return create(key, value, IntegerResponse.class);
    }

    public static RestResponse<LongResponse> create(String key, Long value) {
        return create(key, value, LongResponse.class);
    }

    public static RestResponse<BooleanResponse> create(String msg, String key, Boolean value) {
        return create(msg, key, value, BooleanResponse.class);
    }

    public static RestResponse<StringResponse> create(String msg, String key, String value) {
        return create(msg, key, value, StringResponse.class);
    }

    public static RestResponse<IntegerResponse> create(String msg, String key, Integer value) {
        return create(msg, key, value, IntegerResponse.class);
    }

    public static RestResponse<LongResponse> create(String msg, String key, Long value) {
        return create(msg, key, value, LongResponse.class);
    }

    public static RestResponse<BooleanResponse> create(String key, Boolean value) {
        return create(key, value, BooleanResponse.class);
    }


    private static <T> RestResponse<T>
    create(String key, Object value, Class<T> klass) {
        return create("OK", key, value, klass);
    }

    private static <T> RestResponse<T>
    create(String msg, String key, Object value, Class<T> klass) {

        OneKeyResponse response;
        try {
            response = (OneKeyResponse) klass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("实例化失败", e);
        }
        response.singleton(key, value);
        return RestResponse.success(msg, (T) response);
    }

}
