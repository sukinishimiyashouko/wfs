package com.wbu.services;

import com.wbu.annotation.RateLimiter;

/**
 * @auther 11852
 * @create 2024/3/10
 */
public interface ILimitManager {
    boolean tryAccess(RateLimiter rateLimiter);
}
