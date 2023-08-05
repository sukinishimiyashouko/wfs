package com.wbu.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wbu.util.RequestUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Aspect
@Slf4j
@Component
public class ApiLoggerAspect {
    private final HttpServletRequest request;

    private final ObjectMapper objectMapper;

    public ApiLoggerAspect(HttpServletRequest request, ObjectMapper objectMapper) {
        this.request = request;
        this.objectMapper = objectMapper;
    }
    @Pointcut("execution(* com.wbu.controller..*Controller.*(..))")
    public void apiLog(){

    }

    @Around("apiLog()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        LogModel logModel = new LogModel();
        long start = System.currentTimeMillis();
        Object result = point.proceed();
        //解析请求参数
        parseRequest(point,logModel);
        //设置返回值
        logModel.setResponse(result);
        long end = System.currentTimeMillis();
        logModel.setTimestamp(end)
                .setCost(end-start);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logModel));
        return result;
    }

    @AfterThrowing(value = "apiLog()",throwing = "e")
    public void afterThrowing(JoinPoint point, Throwable e) throws JsonProcessingException {
        long start = System.currentTimeMillis();
        StackTraceElement[] stackTrace = e.getStackTrace();
        log.info(Arrays.toString(stackTrace));
        String stackTracing = Arrays.toString(stackTrace).replace("[", "").replace("]", "");
        StackTraceElement stackTraceElement = e.getStackTrace()[0];
        ExceptionInfo exceptionInfo = new ExceptionInfo();
        exceptionInfo.setMessage(e.getMessage())
                .setFileName(stackTraceElement.getFileName())
                .setClassName(stackTraceElement.getClassName())
                .setMethodName(stackTraceElement.getMethodName())
                .setLineNumber(stackTraceElement.getLineNumber())
                .setDetails(stackTracing);
        LogModel logModel = new LogModel();
        logModel.setException(exceptionInfo);
        //解析请求参数
        parseRequest((ProceedingJoinPoint) point,logModel);
        long end = System.currentTimeMillis();
        logModel.setTimestamp(end)
                .setCost(end-start);
        log.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logModel));
    }

    private void parseRequest(ProceedingJoinPoint point,LogModel logModel){
        String ipAddr = RequestUtil.getIpAddr(request);
        String method = request.getMethod();
        StringBuffer path = request.getRequestURL();
        Object[] args = point.getArgs();
        CodeSignature signature = (CodeSignature) point.getSignature();
        String[] parameterNames = signature.getParameterNames();
        HashMap<String, Object> params = new HashMap<>(parameterNames.length);
        for (int i = 0; i < parameterNames.length; i++) {
            params.put(parameterNames[i],args[i].toString());
        }
        logModel.setIp(ipAddr)
                .setMethod(method)
                .setPath(path.toString())
                .setParams(params);
    }
    @Data
    @Accessors(chain = true)
    public static class LogModel{
        private String ip;
        private String method;
        private String path;
        private Map<String,Object> params;
        private Object response;
        private ExceptionInfo exception;
        private Long timestamp;
        private Long cost;
    }

    @Data
    @Accessors(chain = true)
    public static class ExceptionInfo{
        private String message;
        private String fileName;
        private String className;
        private String methodName;
        private Integer lineNumber;
        private Object details;
    }
}
