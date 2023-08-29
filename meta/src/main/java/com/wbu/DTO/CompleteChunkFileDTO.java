package com.wbu.DTO;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @auther 11852
 * @create 2023/8/2
 */
@Data
public class CompleteChunkFileDTO {
    @NotBlank(message = "文件名不得为空")
    private String fileName;
    @NotNull(message = "分片序号不得为空")
    private Integer chunkNo;
    @NotBlank(message = "存储地址不得为空")
    private String address;
    @NotBlank(message = "通信协议不得为空")
    private String schema;
    @NotBlank(message = "文件md5不得为空")
    private String md5;

}
