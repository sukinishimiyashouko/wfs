package com.wbu.services;

import com.wbu.config.DiscoveryConfig;
import com.wbu.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Service
@Slf4j
public class Actuator {
    private final DiscoveryConfig discoveryConfig;
    private final RestTemplate restTemplate;
    private final ScheduledExecutorService scheduledExecutorService;
    @Value("${server.port}")
    private int port;

    public Actuator(DiscoveryConfig discoveryConfig, RestTemplate restTemplate, ScheduledExecutorService scheduledExecutorService) {
        this.discoveryConfig = discoveryConfig;
        this.restTemplate = restTemplate;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * 在依赖注入完成之后自动执行该方法
     */
    @PostConstruct
    public void register() {
        String serverAddress = discoveryConfig.getServerAddress();
        Map<String,Object> params = new HashMap<>();
        params.put("serviceId",discoveryConfig.getServiceId());
        params.put("host", RequestUtil.getLocalHost());
        params.put("port",port);
        params.put("schema",discoveryConfig.getSchema());

        Map result = restTemplate.postForObject(serverAddress + "/register", params, Map.class);
        log.info("{}",result);
        if (Objects.isNull(result)||!result.get("code").equals(200)){
            throw new RuntimeException("服务注册失败");
        }
        //(Runnable command,long initialDelay,long period,TimeUnit unit)
        //固定速率
        scheduledExecutorService.scheduleAtFixedRate(()->{
            restTemplate.put(discoveryConfig.getServerAddress()+"/heartbeat",params);
        },0,10, TimeUnit.SECONDS);
    }
}
