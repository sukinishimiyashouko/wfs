package com.wbu.DTO;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
public class ServerInfoDTO {
    @NotBlank(message = "服务ID不得为空")
    private String ServiceId;
    @NotBlank(message = "主机ip不得为空")
    private String host;
    @NotBlank(message = "服务端口不得为空")
    private String port;
    //默认http协议
    @NotBlank(message = "通信协议不得为空")
    private String schema = "http";
}
