package com.wbu.utils.impl;

import com.wbu.BO.ServerInfo;
import com.wbu.error.EnumMetaException;
import com.wbu.errors.BusinessException;
import com.wbu.utils.ServerSelector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Slf4j
public class DefaultServerSelector implements ServerSelector {
    @Override
    public List<ServerInfo> select(List<ServerInfo> aliveServers, int count) {
//        log.info("存活的chunk-server服务器数量为--------------------------------"+ aliveServers.size());
//        log.info("分片数量为------------------------------"+ count);
        if (aliveServers.size()<count){
            throw new BusinessException("存活的服务数量 < 分片存储数量",EnumMetaException.NOT_ENOUGH_CHUNK_SERVER);
        }
        int[] indexArray = new int[aliveServers.size()];
        for (int i = 0; i < indexArray.length; i++) {
            indexArray[i]=i;
        }
        // 洗牌法打乱
        // TODO: 2023/8/3 可改进 
        for (int i = 0; i < count; i++) {
            int randomIndex = (int)(Math.random() * aliveServers.size());
            int temp = indexArray[randomIndex];
            indexArray[randomIndex] = indexArray[i];
            indexArray[i] = temp;
        }
        //打乱后加到新集合中并返回
        List<ServerInfo> selectedServers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            selectedServers.add(aliveServers.get(indexArray[i]));
        }
        return selectedServers;
    }

}
