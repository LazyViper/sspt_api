package com.yumi.sspt.goods;

import com.yumi.sspt.auto.Goods;
import com.yumi.sspt.plugin.rest.RestResponse;
import com.yumi.sspt.plugin.utils.Jackson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Goods
 *
 * @author liyuming@foresee.com.cn
 * @version 1.0
 * @time 2018/11/11 0011
 */
@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;


    @RequestMapping(value = {"/goods/getGoods"},method = {RequestMethod.GET,RequestMethod.POST})
    public RestResponse<Goods> getGoods(@Valid Goods goods ){
        System.out.println("test----"+Jackson.toJson(goods));
        Goods regoods = goodsService.sayHello();

        return RestResponse.successData(goods);
    }

}
