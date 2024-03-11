package com.wbu.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @auther 11852
 * @create 2024/3/10
 */
@SpringBootTest
public class RedisConfigTests {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    public void test(){
        String key = "key-test";
        redisTemplate.opsForValue().set(key,123456);
        System.out.println(redisTemplate.opsForValue().get(key));
    }
}
