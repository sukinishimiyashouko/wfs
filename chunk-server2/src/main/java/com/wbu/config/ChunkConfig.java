package com.wbu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
@Configuration
@ConfigurationProperties("chunk")
public class ChunkConfig {

    /**
     * Linux 存储
     */
    private String workSpace = "/usr/chunk-server";

}
