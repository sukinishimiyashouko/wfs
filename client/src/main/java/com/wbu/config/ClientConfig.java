package com.wbu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.context.annotation.Configuration;


/**
 * @auther 11852
 * @create 2023/8/1
 */
@ConfigurationProperties("client")
@Configuration
@Data
public class ClientConfig {
    private String metaServerHost;
    private String metaServerPort;

    public String getMetaServerAddress(){
        return "%s:%s".formatted(metaServerHost,metaServerPort);
    }
}
