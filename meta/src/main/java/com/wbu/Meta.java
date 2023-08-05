package com.wbu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @auther 11852
 * @create 2023/7/31
 */
//开启定时任务
@EnableScheduling
@SpringBootApplication
public class Meta {
    public static void main(String[] args) {
        SpringApplication.run(Meta.class,args);
    }
}
