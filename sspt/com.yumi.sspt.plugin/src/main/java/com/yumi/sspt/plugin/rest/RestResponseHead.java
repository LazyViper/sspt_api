/**
 * Copyright(c) Foresee Science & Technology Ltd.
 */
package com.yumi.sspt.plugin.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;

/**
 * <pre>
 * REST接口响应头信息
 * </pre>
 *
 * @author chenqiang@foresee.com.cn
 * @date 2016年12月1日
 * @version 1.00.00
 */

@JsonInclude(Include.NON_NULL)
public class RestResponseHead implements Serializable {

    /**
     * serialVersionUID:TODO。
     */
    private static final long serialVersionUID = -1879937950907235155L;

    public static final String SUCCESS_CODE = "0";

    public static final String REDIRECT_CODE = "302";

    /**
     * requestId:请求ID（32位UUID）。
     */
    private String requestId;

    /**
     * respCode:响应代码；默认0；（0=成功；其它8位数字=失败）。
     */
    private String errorCode = SUCCESS_CODE;

    /**
     * respMsg:响应代码对应描述信息
     */
    private String errorMsg = null;

    private long timestamp = System.currentTimeMillis();

    /**
     * 服务响应时间
     */
    private long time;

    /**
     * 网关响应时间
     */
    private long gtime;

    /** 流水号 */
    private String errorNo;

    private String sign;


    public RestResponseHead() {

    }

    public RestResponseHead(String requestId, String errorCode, String errorMsg) {
        super();
        this.requestId = requestId;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.timestamp = System.currentTimeMillis();
        this.time = 0;
    }

    public RestResponseHead(String requestId, String errorCode, String errorMsg, long timestamp, long time) {
        super();
        this.requestId = requestId;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.timestamp = timestamp;
        this.time = time;
    }

    public boolean isSuccess() {
        return errorCode == null || errorCode.equals(SUCCESS_CODE) || errorCode.equals(REDIRECT_CODE);
    }

    public boolean isError() {
        return !isSuccess();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getGtime() {
        return gtime;
    }

    public void setGtime(long gtime) {
        this.gtime = gtime;
    }

    public String getErrorNo() {
        return errorNo;
    }

    public void setErrorNo(String errorNo) {
        this.errorNo = errorNo;
    }

    public boolean isRedirect() {
        if (REDIRECT_CODE.equals(errorCode)) {
            return true;
        }

        return false;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
