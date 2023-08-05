package com.wbu.utils;

import com.wbu.DO.FileChunkMeta;
import com.wbu.errors.BusinessException;
import com.wbu.errors.EnumClientException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @auther 11852
 * @create 2023/8/2
 */
@Component
public class ChunkDownloaderStrategy {
    private final Map<String,ChunkDownloader> downloaderMap;
    public ChunkDownloaderStrategy(List<ChunkDownloader> downloaderList){
        this.downloaderMap = new HashMap<>();
        downloaderList.forEach(downloader -> downloaderMap.put(downloader.schema(),downloader));
    }
    public byte[] download(FileChunkMeta chunkMeta){
        String schema = chunkMeta.getSchema();
        ChunkDownloader downloader = downloaderMap.get(schema);
        if (Objects.isNull(downloader)){
            throw new BusinessException(EnumClientException.SCHEMA_DOES_NOT_SUPPORT);
        }
        return downloader.download(chunkMeta);
    }
}
