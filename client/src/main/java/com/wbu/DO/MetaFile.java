package com.wbu.DO;

import lombok.Data;
import lombok.experimental.Accessors;


import java.util.List;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
@Accessors(chain = true)
public class MetaFile {
    private String fileName;
    private String extension;
    private Long fileSize;
    private String bucketName;
    private Integer totalChunk;
    private List<FileChunkMeta> chunks;
}
