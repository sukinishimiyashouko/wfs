package com.wbu.DTO;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Data
@Accessors(chain = true)
public class FileMeta {
    @NotNull(message = "文件大小不能为空")
    @Min(value = 0,message = "文件大小不得<0")
    private Long fileSize;
    @NotBlank(message = "文件后缀名不能为空")
    private String extension;
    @NotBlank(message = "存储桶名不能为空")
    private String bucketName;
}
