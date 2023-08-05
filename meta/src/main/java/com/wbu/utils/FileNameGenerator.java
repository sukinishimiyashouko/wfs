package com.wbu.utils;

import com.wbu.DTO.FileMeta;



/**
 * @auther 11852
 * @create 2023/8/1
 */
public interface FileNameGenerator {
    public String generate(FileMeta fileMeta, Object... args);
}
