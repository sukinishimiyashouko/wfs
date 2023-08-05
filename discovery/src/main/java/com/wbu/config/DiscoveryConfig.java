package com.wbu.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
@Configuration
@ConfigurationProperties("discovery")
@Slf4j
public class DiscoveryConfig {
    private String serverHost;
    //服务id
    private String serviceId;
    private Integer serverPort;
    //协议
    private String schema;

    public String getServerAddress(){
        String formatted = "%s:%s".formatted(serverHost, serverPort);
        log.info(formatted);
        return formatted;
    }

    /**
     * ScheduledExecutorService可以在构造函数中指定多个对应的后台线程数。
     * 此处指定后台线程数
     * @return
     */
    @Bean
    public ScheduledExecutorService scheduledExecutorService(){
        return Executors.newScheduledThreadPool(20);
    }
}
