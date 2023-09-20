package com.wbu.VO;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @auther 11852
 * @create 2023/8/28
 */
@Data
@Accessors(chain = true)
public class BucketVO {
    private String bucketName;
    private List<FileVO> files;
}
