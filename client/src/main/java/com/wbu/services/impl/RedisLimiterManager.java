package com.wbu.services.impl;

import com.wbu.annotation.RateLimiter;
import com.wbu.enums.LimitType;
import com.wbu.errors.BusinessException;
import com.wbu.response.StandardResponse;
import com.wbu.services.ILimitManager;
import com.wbu.util.RequestUtil;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @auther 11852
 * @create 2024/3/10
 */
@Slf4j
@Service
public class RedisLimiterManager implements ILimitManager {
    private final HttpServletRequest request;
    private final DefaultRedisScript<Long> redisScript;
    private final RedisTemplate<String,Object> redisTemplate;

    public RedisLimiterManager(HttpServletRequest request,
                               DefaultRedisScript<Long> redisScript,
                               RedisTemplate<String, Object> redisTemplate) {
        this.request = request;
        this.redisScript = redisScript;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryAccess(RateLimiter rateLimiter) {
        String key = rateLimiter.key();
        if (StringUtils.isBlank(key)){
            throw new BusinessException("redis limiter 的key 不得为空", StandardResponse.ERROR);
        }
        if (rateLimiter.limitType() == LimitType.IP){
            String ip = RequestUtil.getIpAddr(request);
            key = "%s-%s".formatted(key,ip);
        }

        int maxSeconds = rateLimiter.seconds();
        int maxCount = rateLimiter.count();
        ArrayList<String> keys = new ArrayList<>();
        keys.add(key);
        Long currentCount;
        try {
            currentCount = redisTemplate.execute(redisScript,keys,maxSeconds,maxCount);
        }catch (Exception e){
            log.error("执行 lua 脚本失败",e);
            throw new BusinessException("服务不可用 请稍后重试",StandardResponse.ERROR);
        }
        return Objects.nonNull(currentCount)&&currentCount!=0L&&currentCount<=maxCount;
    }
}
