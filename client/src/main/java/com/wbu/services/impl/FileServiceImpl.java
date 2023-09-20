package com.wbu.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wbu.DO.FileChunkMeta;
import com.wbu.DO.MetaFile;
import com.wbu.DTO.CompleteChunkFileDTO;
import com.wbu.DTO.FileChunkDTO;
import com.wbu.DTO.FileMeta;
import com.wbu.VO.BucketVO;
import com.wbu.VO.FileChunkMetaVO;
import com.wbu.VO.MetaFileVo;
import com.wbu.config.ClientConfig;
import com.wbu.errors.BusinessException;
import com.wbu.errors.EnumClientException;
import com.wbu.response.CommonResponse;
import com.wbu.services.FileService;
import com.wbu.util.Md5Util;
import com.wbu.utils.ChunkAddressStrategy;
import com.wbu.utils.ChunkDownloaderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private final RestTemplate restTemplate;
    private final ClientConfig clientConfig;
    private final ObjectMapper objectMapper;
    private final ChunkAddressStrategy chunkAddressStrategy;
    private final ChunkDownloaderStrategy chunkDownloaderStrategy;
//    public static ThreadLocal<CompleteChunkFileDTO> threadLocal = ThreadLocal.withInitial(CompleteChunkFileDTO::new);

    public FileServiceImpl(RestTemplate restTemplate,
                           ClientConfig clientConfig,
                           ObjectMapper objectMapper,
                           ChunkAddressStrategy chunkAddressStrategy,
                           ChunkDownloaderStrategy chunkDownloaderStrategy) {
        this.restTemplate = restTemplate;
        this.clientConfig = clientConfig;
        this.objectMapper = objectMapper;
        this.chunkAddressStrategy = chunkAddressStrategy;
        this.chunkDownloaderStrategy = chunkDownloaderStrategy;
    }

    @Override
    public String upload(String bucketName, MultipartFile file) {
        // 获取文件名
        String originalFilename = file.getOriginalFilename();
        String extension="";
        if (Objects.nonNull(originalFilename)){
            int doIndex = originalFilename.lastIndexOf(".");
            if (doIndex!=-1){
                extension=originalFilename.substring(doIndex+1);
                log.info(extension);
            }
        }

        FileMeta fileMeta = new FileMeta()
                .setBucketName(bucketName)
                .setExtension(extension)
                .setFileSize(file.getSize());

        Object response = restTemplate.postForObject(clientConfig.getMetaServerAddress() + "/meta/generate",
                fileMeta, Object.class);

        /**
         * 类型转换
         */
        CommonResponse<MetaFile> commonResponse = objectMapper
                .convertValue(response, new TypeReference<>() {
                });

        if (Objects.isNull(commonResponse)){
            throw new BusinessException(EnumClientException.FAILED_TO_GET_META_FILE);
        }
        MetaFile metaFile = commonResponse.getData();
        if (Objects.isNull(metaFile)){
            throw new BusinessException("meta file 为空",EnumClientException.FAILED_TO_GET_META_FILE);
        }

        try {
            uploadChunks(file,metaFile);
        } catch (Exception e) {
            throw new BusinessException(EnumClientException.FAILED_TO_UPLOAD_CHUNK_FILE);
        }
        return "%s/%s.%s".formatted(bucketName,metaFile.getFileName(),metaFile.getExtension());
    }
    private void uploadChunks(MultipartFile file,MetaFile metaFile) throws Exception {
        //获取分片信息
        List<FileChunkMeta> chunks = metaFile.getChunks();

        chunks = chunks.stream()
                .sorted(Comparator.comparing(FileChunkMeta::getChunkNo))
                .collect(Collectors.toList());

        if (chunks.size()==0){
            return;
        }
        InputStream inputStream = file.getInputStream();
        //并行执行
        CompletableFuture<?>[] tasks = new CompletableFuture[chunks.size()];

        byte[] buffer = new byte[0];
        int preChunkNo = -1;
        for (int i = 0; i < chunks.size(); i++) {

            FileChunkMeta chunk = chunks.get(i);
            FileChunkDTO fileChunkDTO = new FileChunkDTO();
            Integer chunkSize = chunk.getChunkSize();
            //读取分片的内容 因为“备份”的作用存在，所以相同序号的分片内容一样就不需要重复读
            if (chunk.getChunkNo() != preChunkNo) {
                preChunkNo = chunk.getChunkNo();
                buffer = new byte[chunkSize];
                inputStream.read(buffer);
            }

            byte[] finalBuffer = buffer;

            tasks[i] = CompletableFuture.runAsync(() -> {
                if (chunk.getIsCompleted()) {
                    return;
                }

                String md5 = Md5Util.getMd5(finalBuffer);

                fileChunkDTO.setFileName(chunk.getFileName())
                        .setExtension(chunk.getExtension())
                        .setChunkNo(chunk.getChunkNo())
                        .setChunkSize(chunkSize)
                        .setBucketName(chunk.getBucketName())
                        .setBytes(finalBuffer);

                String address = chunkAddressStrategy.get(chunk);
                Object response = restTemplate.postForObject(address + "/file/write", fileChunkDTO, Object.class);
                log.info("response:{}",response);
                if (Objects.isNull(response)) {
                    throw new BusinessException("第" + chunk.getChunkNo() + "分片上传失败", EnumClientException.FAILED_TO_UPLOAD_CHUNK_FILE);
                }

                CommonResponse<String> md5Response = objectMapper.convertValue(response, new TypeReference<>() {
                });

                if (!md5Response.getData().equals(md5)) {
                    log.info("md5异常");
                    throw new BusinessException(EnumClientException.CHUNK_FILE_INCOMPLETE);
                }
                CompleteChunkFileDTO completeChunkFileDTO = new CompleteChunkFileDTO();
                completeChunkFileDTO.setFileName(chunk.getFileName())
                        .setChunkNo(chunk.getChunkNo())
                        .setAddress(chunk.getAddress())
                        .setSchema(chunk.getSchema())
                        .setMd5(md5);

                Object resp = restTemplate.postForObject(
                        clientConfig.getMetaServerAddress() + "/meta/chunk/complete",
                        completeChunkFileDTO,
                        Object.class
                );
                if (Objects.isNull(resp)) {
                    throw new BusinessException(EnumClientException.FAILED_TO_UPDATE_CHUNK_FILE_COMPLETE_STATUS);
                }
                log.info("更新分片状态:{}", resp);
            }).whenComplete((o, throwable) -> {
                if (Objects.nonNull(throwable)) {
                    throw new RuntimeException(MessageFormat.format("第{}分片上传失败", chunk.getChunkNo()));
                }
            });


        }
        CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks);
        //如果任务失败，应当快速失败
        CompletableFuture<?> anyException = new CompletableFuture<>();
        Arrays.stream(tasks).forEach(t -> {
            t.exceptionally(throwable -> {
                anyException.completeExceptionally(throwable);
                return null;
            });
        });
        CompletableFuture.anyOf(allOf,anyException).get();
    }


    /**
     * 从Meta获取
     * @param bucketName
     * @param fileName
     * @return
     */
    @Override
    public MetaFile getMeta(String bucketName, String fileName) {
        if (fileName.contains(".")){
            fileName=fileName.split("\\.")[0];
        }
        String url = clientConfig.getMetaServerAddress() + "/meta/info?bucketName={bucketName}&fileName={fileName}";
        //保存参数
        HashMap<String, Object> params = new HashMap<>();
        params.put("bucketName",bucketName);
        params.put("fileName",fileName);

        Object response = restTemplate.getForObject(url, Object.class, params);
        CommonResponse<MetaFile> commonResponse = objectMapper.convertValue(response, new TypeReference<>() {
        });
        return commonResponse.getData();
    }

    @Override
    public byte[] downloadChunk(FileChunkMeta chunk) {
        return chunkDownloaderStrategy.download(chunk);
    }

    @Override
    public MetaFileVo meta(FileMeta fileMeta) {
        String url = clientConfig.getMetaServerAddress()+"/meta/generate";
        Object response = restTemplate.postForObject(url, fileMeta, Object.class);
        CommonResponse<MetaFile> commonResponse = objectMapper.convertValue(response, new TypeReference<>() {
        });
        MetaFile metaFile = commonResponse.getData();
        return buildMetaFileVO(metaFile);
    }
    private MetaFileVo buildMetaFileVO(MetaFile metaFile){
        MetaFileVo metaFileVo = new MetaFileVo();
        //获取分片
        List<FileChunkMeta> originChunks = metaFile.getChunks();

        List<FileChunkMetaVO> fileChunkMetaVOS = new ArrayList<>();
        for (FileChunkMeta originChunk : originChunks) {
            FileChunkMetaVO fileChunkMetaVO = new FileChunkMetaVO();
            fileChunkMetaVO.setFileName(originChunk.getFileName())
                    .setChunkNo(originChunk.getChunkNo())
                    .setChunkStart(originChunk.getChunkStart().intValue())
                    .setChunkSize(originChunk.getChunkSize())
                    .setCompleted(originChunk.getIsCompleted());
            fileChunkMetaVOS.add(fileChunkMetaVO);
        }
        //筛选
        fileChunkMetaVOS = fileChunkMetaVOS.stream().distinct().collect(Collectors.toList());
        return metaFileVo.setChunks(fileChunkMetaVOS)
                .setFileName(metaFileVo.getFileName())
                .setBucketName(metaFileVo.getBucketName());
    }

    @Override
    public String uploadChunk(String bucketName,
                              String fileName,
                              String md5,
                              Integer chunkNo,
                              MultipartFile file) {
//        MetaFile metaFile = getMeta(bucketName, fileName);
//        List<FileChunkMeta> chunks = metaFile.getChunks();
        String metaServerAddress = clientConfig.getMetaServerAddress();
        String url = metaServerAddress + "/meta/chunk/info?bucketName={bucketName}&fileName={fileName}&chunkNo={chunkNo}";
        Map<String, Object> map = new HashMap<>();
        map.put("fileName",fileName);
        map.put("bucketName",bucketName);
        map.put("chunkNo",chunkNo);
        Object resp = restTemplate.getForObject(url,Object.class,map);
        CommonResponse<List<FileChunkMeta>> chunkInfoResponse = objectMapper.convertValue(resp, new TypeReference<>() {
        });
        List<FileChunkMeta> chunks = chunkInfoResponse.getData();
        String realMd5 = Md5Util.getMd5(file);
        if (!Objects.equals(md5,realMd5)){
            throw new BusinessException(EnumClientException.CHUNK_FILE_INCOMPLETE);
        }
        chunks=chunks.stream().filter(c->c.getChunkNo().equals(chunkNo)).collect(Collectors.toList());
        chunks.forEach(c->{
            int chunkSize = c.getChunkSize();
            byte[] buffer = new byte[chunkSize];
            try {
                InputStream inputStream = file.getInputStream();
                inputStream.read(buffer);

                FileChunkDTO fileChunkDTO = new FileChunkDTO();
                fileChunkDTO.setChunkNo(c.getChunkNo())
                        .setFileName(c.getFileName())
                        .setExtension(c.getExtension())
                        .setChunkSize(c.getChunkSize())
                        .setBucketName(c.getBucketName())
                        .setBytes(buffer);
                String address = chunkAddressStrategy.get(c);
                Object response = restTemplate.postForObject(address + "/file/write",
                        fileChunkDTO,
                        Object.class);
                if (Objects.isNull(response)){
                    throw new RuntimeException(MessageFormat.format("第{}分片上传失败",c.getChunkNo()));
                }
                CommonResponse<String> stringCommonResponse = objectMapper.convertValue(response, new TypeReference<CommonResponse<String>>() {
                });
                String serverMd5 = stringCommonResponse.getData();
                if (!Objects.equals(serverMd5,realMd5)){
                    throw new RuntimeException(MessageFormat.format("第{}分片不完整",c.getChunkNo()));
                }

                CompleteChunkFileDTO completeChunkFileDTO = new CompleteChunkFileDTO();
                completeChunkFileDTO.setFileName(c.getFileName())
                        .setChunkNo(c.getChunkNo())
                        .setSchema(c.getSchema())
                        .setMd5(md5)
                        .setAddress(c.getAddress());

                Object completeResp = restTemplate.postForObject(metaServerAddress + "/meta/chunk/complete",
                        completeChunkFileDTO,
                        Object.class
                );
                if (Objects.isNull(completeResp)){
                    throw new RuntimeException(MessageFormat.format("第{}分片更新失败",c.getChunkNo()));
                }

            } catch (IOException e) {
                log.info("第{}分片上传失败,原因是",c.getChunkNo(),e);
                throw new BusinessException(EnumClientException.FAILED_TO_UPLOAD_CHUNK_FILE);
            }

        });
        return md5;
    }

    @Override
    public List<BucketVO> files() {
        String metaServerAddress = clientConfig.getMetaServerAddress();
        Object response = restTemplate.getForObject(metaServerAddress + "/meta/files", Object.class);
        CommonResponse<List<BucketVO>> commonResponse = objectMapper.convertValue(response, new TypeReference<CommonResponse<List<BucketVO>>>() {
        });
        return commonResponse.getData();
    }

    @Override
    public void delete(String bucketName, String fileName) {
        String metaServerAddress = clientConfig.getMetaServerAddress();
        String url = "%s/meta/%s/%s".formatted(metaServerAddress,bucketName,fileName);
        restTemplate.delete(url);
    }


}
