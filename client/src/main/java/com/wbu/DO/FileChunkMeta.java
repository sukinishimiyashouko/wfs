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
    private String fileName;
    private String extension;
    private Integer chunkNo;
    private String bucketName;
    private Long chunkStart;
    private Integer chunkSize;
    private String address;
    private String chunkMd5;
    private String schema;
    private Boolean isCompleted = false;
}
