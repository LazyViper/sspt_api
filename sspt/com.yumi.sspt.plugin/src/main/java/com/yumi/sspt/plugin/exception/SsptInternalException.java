package com.yumi.sspt.plugin.exception;

/**
 * FtcspInternalException
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.0
 */
public class SsptInternalException extends RuntimeException {

    private String errorCode;

    private String errorMsg;

    private Throwable throwable;

    public SsptInternalException(String errorCode, String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.throwable = throwable;
    }

    public SsptInternalException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public SsptInternalException setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public SsptInternalException setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }
}
