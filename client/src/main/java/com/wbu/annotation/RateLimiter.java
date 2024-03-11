package com.wbu.annotation;

import com.wbu.enums.LimitType;

import java.lang.annotation.*;

/**
 * @auther 11852
 * @create 2024/3/10
 */
@Documented
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    String key() default "limit";
    int seconds() default 60;

    int count() default 100;
    String message() default "访问过于频繁，请稍后重复";

    LimitType limitType() default LimitType.DEFAULT;
}
