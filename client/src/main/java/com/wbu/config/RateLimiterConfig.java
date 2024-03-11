package com.wbu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * @auther 11852
 * @create 2024/3/10
 */
@Data
@Configuration
@ConfigurationProperties("rate-limit")
public class RateLimiterConfig {
    private Integer maxRequestCount;

    @Bean
    public DefaultRedisScript<Long> redisScript(){
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/limit.lua")));
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}
