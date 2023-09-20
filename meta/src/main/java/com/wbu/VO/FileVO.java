package com.wbu.VO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @auther 11852
 * @create 2023/8/28
 */
@Data
@Accessors(chain = true)
public class FileVO {
    private String fileName;
    private String extension;
    private String bucketName;
    private Long fileSize;
}
