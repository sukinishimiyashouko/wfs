package com.wbu.utils;

import com.wbu.BO.ServerInfo;

import java.util.List;

/**
 * @auther 11852
 * @create 2023/8/1
 */
public interface ServerSelector {
    List<ServerInfo> select(List<ServerInfo> aliveServers,int count);
}
