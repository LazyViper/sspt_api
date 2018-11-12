/**
 * Copyright(c) Foresee Science & Technology Ltd.
 */
package com.yumi.sspt.plugin.exception;
/**
 * <pre>
 * TODO。
 * </pre>
 *
 * @author Administrator@foresee.com.cn
 * @date 2016年12月1日
 * @version 1.00.00
 * 
 *          <pre>
 * 修改记录 
 *    修改后版本:     修改人：  修改日期:     修改内容:
 *          </pre>
 */

public class SsptRuntimeException extends RuntimeException {
    
    /**
     * serialVersionUID:TODO。
     */
    private static final long serialVersionUID = 6358803642758332780L;

    private String            errorCode;

    private String            errorMsg;
    
    private Object[]          params;


    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
    

    
    public Object[] getParams() {
        return params;
    }

    public SsptRuntimeException(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, null);
    }

    public SsptRuntimeException(String errorCode, String errorMsg, Throwable t) {
        super(errorCode + ":" + errorMsg, t);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }
    /**
     * 
     * Create a new instance of FtcspRuntimeException.
     * @param errorCode:业务异常code值
     */
    public SsptRuntimeException(String errorCode)
    {
        this(errorCode,new Object[]{});
    }
    /**
     * 
     * Create a new instance of FtcspRuntimeException.
     * @param errorCode:业务异常code值
     * @param params   :异常消息参数,多个参数用逗号分开
     *        如:zhangsan,123456,18
     */
    public SsptRuntimeException(String errorCode, Object ...params)
    {
        this(errorCode,null,params);
    }
    /**
     * 
     * Create a new instance of FtcspRuntimeException
     *@param errorCode:业务异常code值
     *@param t        :异常信息
     *@param params   :异常消息参数,多个参数用逗号分开
     *        如:zhangsan,123456,18
     */
    public SsptRuntimeException(String errorCode, Throwable t, Object ...params)
    {
        super(errorCode, t);
        this.errorCode=errorCode;
        this.params=params;
        
    }
    
}
