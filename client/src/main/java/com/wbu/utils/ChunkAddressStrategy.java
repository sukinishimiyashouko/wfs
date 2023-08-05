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
public class ChunkAddressStrategy {
    private final Map<String,ChunkAddressBuilder> builderMap;

    public ChunkAddressStrategy(List<ChunkAddressBuilder> builderList){
        this.builderMap = new HashMap<>();
        builderList.forEach(builder -> builderMap.put(builder.schema(),builder));
    }

    public String get(FileChunkMeta chunkMeta){
        String schema = chunkMeta.getSchema();
        ChunkAddressBuilder chunkAddressBuilder = builderMap.get(schema);
        if (Objects.isNull(chunkAddressBuilder)){
            throw new BusinessException(EnumClientException.SCHEMA_DOES_NOT_SUPPORT);
        }
        return chunkAddressBuilder.build(chunkMeta);
    }
}
