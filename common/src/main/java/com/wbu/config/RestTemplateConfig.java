package com.wbu.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * @auther 11852
 * @create 2023/8/2
 */
@Configuration
public class RestTemplateConfig {

    private final RestTemplateErrorHandler restTemplateErrorHandler;
    private final RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;

    public RestTemplateConfig(RestTemplateErrorHandler restTemplateErrorHandler, RestTemplateLoggingInterceptor restTemplateLoggingInterceptor) {
        this.restTemplateErrorHandler = restTemplateErrorHandler;
        this.restTemplateLoggingInterceptor = restTemplateLoggingInterceptor;
    }

    /**
     * 配置RestTemplate
     * RestTemplateBuilder 自定义连接参数
     * @param restTemplateBuilder
     * @param clientHttpRequestFactory
     * @return
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder,
                                     ClientHttpRequestFactory clientHttpRequestFactory){
        //构造类构造RestTemplate
        RestTemplate restTemplate = restTemplateBuilder.build();
        //自定义拦截器
        restTemplate.setInterceptors(Collections.singletonList(restTemplateLoggingInterceptor));
        //自定义异常处理器
        restTemplate.setErrorHandler(restTemplateErrorHandler);
        //通过BufferingClientHttpRequestFactory对象包装现有的ResquestFactory，用来支持多次调用getBody()方法获取结果
        //将输入流和输出流保存到内存中，允许多次读取
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(clientHttpRequestFactory));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory ClientHttpRequestFactory(){

        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();

        simpleClientHttpRequestFactory.setConnectTimeout(150000);

        simpleClientHttpRequestFactory.setReadTimeout(15000);
        return simpleClientHttpRequestFactory;
    }

}
