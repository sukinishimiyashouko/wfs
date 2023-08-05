package com.wbu.utils;

import com.wbu.DO.FileChunkMeta;

/**
 * @auther 11852
 * @create 2023/8/2
 */
public interface ChunkDownloader {
    byte[] download(FileChunkMeta chunkMeta);

    String schema();
}
