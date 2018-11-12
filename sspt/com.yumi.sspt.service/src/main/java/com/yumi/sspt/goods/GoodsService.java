package com.yumi.sspt.goods;

import com.yumi.sspt.auto.Goods;
import org.springframework.stereotype.Service;

/**
 * GoodsService
 *
 * @author liyuming@foresee.com.cn
 * @version 1.0
 * @time 2018/11/11 0011
 */
@Service
public class GoodsService {



    public Goods sayHello(){
        Goods goods = new Goods();
        goods.setName("hello");
        return goods;
    }

}
