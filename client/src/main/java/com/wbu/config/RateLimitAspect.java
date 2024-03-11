package com.wbu.config;

import com.wbu.annotation.RateLimiter;
import com.wbu.errors.BusinessException;
import com.wbu.response.StandardResponse;
import com.wbu.services.ILimitManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @auther 11852
 * @create 2024/3/10
 */


@Aspect
@Component
public class RateLimitAspect {
    private final ILimitManager manager;

    public RateLimitAspect(ILimitManager manager) {
        this.manager = manager;
    }

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter){
        if (!manager.tryAccess(rateLimiter)){
            throw new BusinessException(rateLimiter.message(), StandardResponse.ERROR);
        }
    }
}
