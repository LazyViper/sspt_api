package com.yumi.sspt.auto;

import javax.validation.constraints.NotBlank;

/**
 * Goods
 *
 * @author liyuming@foresee.com.cn
 * @version 1.0
 * @time 2018/11/11 0011
 */
public class Goods {

    @NotBlank(message = "id不能为空")
    private String id;

    private String name;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
