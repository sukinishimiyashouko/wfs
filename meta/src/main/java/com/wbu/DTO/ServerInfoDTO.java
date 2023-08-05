package com.wbu.DTO;

import lombok.Data;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
public class ServerInfoDTO {
    private String ServiceId;
    private String host;
    private String port;
    //默认http协议
    private String schema = "http";
}
