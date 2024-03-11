package com.wbu.utils.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.wbu.DTO.FileMeta;
import com.wbu.utils.FileNameGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @auther 11852
 * @create 2023/8/1
 */
public class DefaultFileNameGenerator implements FileNameGenerator {
    @Override
    public String generate(FileMeta fileMeta, Object... args) {
        //获取当前时间戳
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String time = now.format(formatter);

        return "%s_%s%s".formatted(fileMeta.getNewFileName(),time,md5(fileMeta,(String) args[0]));
    }

    private String md5(FileMeta fileMeta,String address){
        return DigestUtil.md5Hex(
                "%s_%s_%s.%s".formatted(address,fileMeta.getFileSize(),fileMeta.getBucketName(),fileMeta.getExtension())
        );
    }
}
