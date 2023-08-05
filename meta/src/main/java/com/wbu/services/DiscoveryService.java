package com.wbu.services;

import com.wbu.BO.ServerInfo;
import com.wbu.DTO.ServerInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * @auther 11852
 * @create 2023/8/1
 */
public interface DiscoveryService {

    /**
     * 服务注册
     * @param serverInfoDTO
     */
    void register(ServerInfoDTO serverInfoDTO);

    void heartbeat(ServerInfoDTO serverInfo);

    Map<String, List<ServerInfo>> services();

    List<ServerInfo> aliveServers();
}
