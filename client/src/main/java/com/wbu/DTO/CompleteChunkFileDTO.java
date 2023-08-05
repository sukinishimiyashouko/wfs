package com.wbu.DTO;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @auther 11852
 * @create 2023/8/2
 */
@Data
@Accessors(chain = true)
public class CompleteChunkFileDTO {

    private String fileName;
    private Integer chunkNo;
    private String address;
    private String schema;
    private String md5;

}
