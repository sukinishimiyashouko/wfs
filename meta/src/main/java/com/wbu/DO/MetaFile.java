package com.wbu.DO;

import io.github.classgraph.json.Id;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
@Document("meta")
@Accessors(chain = true)
public class MetaFile {
    //文件名也是文档ID
    @Id
    private String fileName;
    //文件后缀名
    private String extension;
    //文件大小
    private Long fileSize;
    //存储桶名
    private String bucketName;
    //分片总数
    private Integer totalChunk;
    //文件总分片
    private List<FileChunkMeta> chunks;
}
