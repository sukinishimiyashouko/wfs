package com.wbu.DTO;

import lombok.Data;

/**
 * @auther 11852
 * @create 2023/8/2
 */
@Data
public class CompleteChunkFileDTO {

    private String fileName;
    private Integer chunkNo;
    private String address;
    private String schema;
    private String md5;

}
