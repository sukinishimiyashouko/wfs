package com.wbu.errors;

import com.wbu.interfaces.IResponse;

/**
 * @auther 11852
 * @create 2023/8/1
 */
public enum ChunkException implements IResponse {
    FAILED_TO_CREATED_FILE(2001,"分片文件创建失败"),
    DISK_SPACE_NOT_ENOUGH_MEMORY(2002,"磁盘空间不足"),
    FAILED_TO_READ_CHUNK_FILE(2003,"分片文件读取失败")
    ;

    private final int code;
    private final String message;

    ChunkException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
