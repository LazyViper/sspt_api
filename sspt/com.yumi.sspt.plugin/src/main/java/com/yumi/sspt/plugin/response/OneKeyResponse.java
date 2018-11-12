/*
* Copyright（c） Foresee Science & Technology Ltd.
*/

package com.yumi.sspt.plugin.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.yumi.sspt.plugin.rest.RestResponse;
import com.yumi.sspt.plugin.rest.RestResponseHead;
import com.yumi.sspt.plugin.utils.Jackson;


import java.util.Collections;
import java.util.Map;

/**
 * <pre>
 *
 * </pre>
 *
 * @author chenwenlong@foresee.com.cn
 * @version 1.00.00
 * @date 2017年08月21日
 */
public class OneKeyResponse<V> {

    protected Map<String, Object> oneKeyMap = Collections.emptyMap();

    public void singleton(String key, V value) {
        oneKeyMap = Collections.singletonMap(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return oneKeyMap;
    }

    public static void main(String[] args) {
        RestResponse<LongResponse> goodsId = RestResponse.create("goodsId", Long.MAX_VALUE);
        RestResponseHead head = new RestResponseHead();
        head.setGtime(System.currentTimeMillis());
        goodsId.setHead(head);
        System.out.println(Jackson.toJson(goodsId));
    }

}
