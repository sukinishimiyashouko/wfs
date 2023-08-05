package com.wbu.DTO;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
@Accessors(chain = true)
public class FileChunkDTO {
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    @NotBlank(message = "文件后缀名不能为空")
    private String extension;
    @NotNull(message = "文件分片序号不能为空")
    private Integer chunkNo;
    @NotNull(message = "文件分片大小不能为空")
    private Integer chunkSize;
    @NotBlank(message = "文件存储桶不能为空")
    private String bucketName;
    @NotNull(message = "文件内容不能为空")
    private byte[] bytes;

}
