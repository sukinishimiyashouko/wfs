package com.wbu.VO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @auther 11852
 * @create 2023/8/2
 */
@Data
@Accessors(chain = true)
public class FileChunkMetaVO {

    private String fileName;
    private Integer chunkNo;
    private Integer chunkStart;
    private Integer chunkSize;
    private Boolean completed;

}
