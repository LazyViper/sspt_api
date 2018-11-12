package com.yumi.sspt.plugin.filter;

/**
 * <pre>
 *  httpserveltrequest filter的顺序
 * </pre>
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.00.00
 * @date 2017年08月12日
 * <p>
 * <pre>
 * 修改记录
 * 修改后版本: 修改人： 修改日期: 修改内容:
 * </pre>
 */
public interface FilterOrder {

    int LOG = 8;
    int REMOTE_IP = 9;
    int REPEATABLE = 10;
    int ASYNC = 11;
}
