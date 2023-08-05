package com.wbu.VO;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @auther 11852
 * @create 2023/8/2
 */
@Accessors(chain = true)
@Data
public class MetaFileVo {
    private String fileName;
    private String bucketName;
    List<FileChunkMetaVO> chunks;
}
