package com.wbu.services;

import com.wbu.DO.FileChunkMeta;
import com.wbu.DO.MetaFile;
import com.wbu.DTO.CompleteChunkFileDTO;
import com.wbu.DTO.FileMeta;

import java.util.List;

/**
 * @auther 11852
 * @create 2023/8/1
 */
public interface MetaService {
    MetaFile generate(FileMeta fileMeta);

    void completeChunk(CompleteChunkFileDTO completeChunkFileDTO);

    MetaFile meta(String bucketName, String fileName);

    List<FileChunkMeta> chunkInfo(String bucketName, String fileName, Integer chunkNo);
}
