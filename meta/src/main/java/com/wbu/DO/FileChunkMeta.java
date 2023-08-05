package com.wbu.DO;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
@Accessors(chain = true)
public class FileChunkMeta {
    //文件名
    private String fileName;
    //后缀名
    private String extension;
    //分片序号
    private Integer chunkNo;
    //存储桶名
    private String bucketName;
    //分片初始分片大小
    private Long chunkStart;
    //分片大小
    private Integer chunkSize;
    //分片地址
    private String address;
    //md5校验 判断分片是否完整
    private String chunkMd5;
    //协议
    private String schema;
    //是否完成
    private Boolean isCompleted = false;
    //权重
    private Integer weight;
}
