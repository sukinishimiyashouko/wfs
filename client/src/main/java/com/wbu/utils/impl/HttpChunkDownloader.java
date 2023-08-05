package com.wbu.utils.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wbu.DO.FileChunkMeta;
import com.wbu.DO.MetaFile;
import com.wbu.response.CommonResponse;
import com.wbu.utils.ChunkAddressStrategy;
import com.wbu.utils.ChunkDownloader;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * @auther 11852
 * @create 2023/8/2
 */
@Service
public class HttpChunkDownloader implements ChunkDownloader {

    private final ChunkAddressStrategy chunkAddressStrategy;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HttpChunkDownloader(ChunkAddressStrategy chunkAddressStrategy, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.chunkAddressStrategy = chunkAddressStrategy;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] download(FileChunkMeta chunkMeta) {
        String address = chunkAddressStrategy.get(chunkMeta);

        String url = address + "/file/read?fileName={fileName}&extension={extension}&chunkNo={chunkNo}&bucketName={bucketName}";
        HashMap<String, Object> params = new HashMap<>();
        params.put("fileName",chunkMeta.getFileName());
        params.put("extension",chunkMeta.getExtension());
        params.put("chunkNo",chunkMeta.getChunkNo());
        params.put("bucketName",chunkMeta.getBucketName());
        Object response = restTemplate.getForObject(url, Object.class, params);
        CommonResponse<byte[]> commonResponse = objectMapper.convertValue(response, new TypeReference<CommonResponse<byte[]>>() {
        });
        return commonResponse.getData();
    }

    @Override
    public String schema() {
        return "http";
    }
}
