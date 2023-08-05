package com.wbu.config;

import com.wbu.utils.FileNameGenerator;
import com.wbu.utils.ServerSelector;
import com.wbu.utils.impl.DefaultFileNameGenerator;
import com.wbu.utils.impl.DefaultServerSelector;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
@Configuration
@ConfigurationProperties("meta")
public class MetaConfig {
    private Integer chunkSize;
    //分片存储数量
    private Integer chunkInstanceCount;
    private Boolean useHttps = false;
    private Integer chunkInstanceMaxWeight = 16;

    @Bean
    @ConditionalOnMissingBean(value = FileNameGenerator.class)
    public FileNameGenerator fileNameGenerator(){
        return new DefaultFileNameGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(value = ServerSelector.class)
    public ServerSelector serverSelector(){
        return new DefaultServerSelector();
    }
}
