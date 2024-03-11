package com.wbu.services.impl;

import com.wbu.BO.ServerInfo;
import com.wbu.DTO.ServerInfoDTO;
import com.wbu.services.DiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 非持久化
 * @auther 11852
 * @create 2023/8/1
 */
@Service
@Slf4j
public class DefaultDiscoveryServiceImpl implements DiscoveryService {
    private final Map<String, List<ServerInfo>> SERVER_MAP = new ConcurrentHashMap<>();
    @Override
    public void register(ServerInfoDTO serverInfo) {
        //获取 SERVER_MAP 里面是否存在server信息
        List<ServerInfo> serverInfos = SERVER_MAP.getOrDefault(serverInfo.getServiceId(), new ArrayList<>());
        //如果不存在，直接放入
        ServerInfo info = new ServerInfo();
        BeanUtils.copyProperties(serverInfo,info);
        //设置注册时间戳
        info.setPreTimeStamp(System.currentTimeMillis());
        /**
         * 此处一定要重写hashCode()和equals()
         */
        if (!serverInfos.contains(info)){
            serverInfos.add(info);
        }
        //注册执行
        SERVER_MAP.put(serverInfo.getServiceId(),serverInfos);
    }

    /**
     * 接收心跳包
     * @param serverInfo
     */
    @Override
    public void heartbeat(ServerInfoDTO serverInfo) {
        //获取服务信息
        List<ServerInfo> serverInfoList = SERVER_MAP.getOrDefault(serverInfo.getServiceId(), new ArrayList<>());
        boolean exist = false;
        for (ServerInfo server : serverInfoList) {
            if (server.getHost().equals(serverInfo.getHost()) &&
                    server.getPort().equals(serverInfo.getPort())) {
                server.setAlive(true);
                server.setPreTimeStamp(System.currentTimeMillis());
                exist = true;
            }
        }
        if (!exist) {
            register(serverInfo);
        }
    }

    /**
     * 设置定时任务 / 表示递增触发，此处是每10s执行一次
     */
    @Scheduled(cron = "*/10 * * * * *")
    private void checkAlive() {
        SERVER_MAP.forEach((serviceId, serverList) -> {
            serverList = serverList.stream().filter(server -> {
                long preTimeStamp = server.getPreTimeStamp() / 1000;
                long current = System.currentTimeMillis() / 1000;
                if (current - preTimeStamp > 30) {
                    server.setAlive(false);
                }
                return current - preTimeStamp < 60;
            }).collect(Collectors.toList());
            SERVER_MAP.put(serviceId, serverList);
        });
        log.info("check server status end");
    }

    @Override
    public Map<String, List<ServerInfo>> services() {
        return SERVER_MAP;
    }

    @Override
    public List<ServerInfo> aliveServers() {
        /**
         * 判断服务器是否存活，此处应该修改，测试阶段使用
         */
        // TODO: 2023/8/2  
        List<ServerInfo> chunkServers = SERVER_MAP.getOrDefault("chunk-server", new ArrayList<>());
        return chunkServers
                .stream()
                .filter(ServerInfo::getAlive)
                .collect(Collectors.toList());
    }
}
