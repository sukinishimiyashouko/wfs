package com.wbu.utils.impl;

import com.wbu.DO.FileChunkMeta;
import com.wbu.utils.ChunkAddressBuilder;
import org.springframework.stereotype.Component;

/**
 * @auther 11852
 * @create 2023/8/2
 */
@Component
public class HttpsChunkAddressBuilder implements ChunkAddressBuilder {
    @Override
    public String build(FileChunkMeta chunkMeta) {
        return "%s://%s".formatted(chunkMeta.getSchema(),chunkMeta.getAddress());
    }

    @Override
    public String schema() {
        return "https";
    }
}
