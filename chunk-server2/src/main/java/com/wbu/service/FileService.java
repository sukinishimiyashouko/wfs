package com.wbu.service;

import com.wbu.DTO.FileChunkDTO;

/**
 * @auther 11852
 * @create 2023/8/1
 */
public interface FileService {

    String write(FileChunkDTO fileChunkDTO);

    byte[] read(String fileName, String extension, Integer chunkNo, String bucketName);
}
