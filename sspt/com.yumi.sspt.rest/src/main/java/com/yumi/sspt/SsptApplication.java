package com.yumi.sspt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SsptApplication
 *
 * @author liyuming@foresee.com.cn
 * @version 1.0
 * @time 2018/11/11 0011
 */
@SpringBootApplication(scanBasePackages = {"com.yumi.sspt.*"})
public class SsptApplication {

    public static void main(String[] args) {
        // 切换JsonUtils为Jackson
        SpringApplication.run(SsptApplication.class, args);
    }
}
