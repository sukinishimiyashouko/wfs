package com.wbu.service.impl;

import com.wbu.DTO.FileChunkDTO;
import com.wbu.config.ChunkConfig;
import com.wbu.errors.BusinessException;
import com.wbu.errors.ChunkException;
import com.wbu.service.FileService;
import com.wbu.util.Md5Util;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Service
public class FileServiceImpl implements FileService {
    private final ChunkConfig chunkConfig;
    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    public FileServiceImpl(ChunkConfig chunkConfig) {
        this.chunkConfig = chunkConfig;
    }

    @Override
    public String write(FileChunkDTO fileChunkDTO) {
        String fileName = fileChunkDTO.getFileName();
        String bucketName = fileChunkDTO.getBucketName();
        Integer chunkSize = fileChunkDTO.getChunkSize();
        Integer chunkNo = fileChunkDTO.getChunkNo();
        String extension = fileChunkDTO.getExtension();
        byte[] bytes = fileChunkDTO.getBytes();

        String chunkPath = buildChunkPath(bucketName, fileName, chunkNo, extension);
        File chunkFile = new File(chunkPath);
        try(FileOutputStream fileOutputStream = new FileOutputStream(chunkFile)){
            reentrantReadWriteLock.writeLock().lock();
            /**
             * 判断磁盘空间是否充足
             */
            if (chunkFile.getFreeSpace()<chunkSize){
                throw new BusinessException(ChunkException.DISK_SPACE_NOT_ENOUGH_MEMORY);
            }
            /**
             * 判断分片是否存在
             */
            if (!chunkFile.exists()){
                boolean created = chunkFile.createNewFile();
                //创建失败
                if (!created){
                    throw new BusinessException(ChunkException.FAILED_TO_CREATED_FILE);
                }
            }
            fileOutputStream.write(bytes);
            String md5 = Md5Util.getMd5(bytes);
            return md5;
        }catch (Exception e){
            throw new BusinessException(ChunkException.FAILED_TO_CREATED_FILE);
        }finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    @Override
    public byte[] read(String fileName, String extension, Integer chunkNo, String bucketName) {
        String chunkPath = buildChunkPath(bucketName, fileName, chunkNo, extension);
        try{
            reentrantReadWriteLock.readLock().lock();
            /**
             * 读取文件
             */
            return Files.readAllBytes(Paths.get(chunkPath));
        }catch (Exception e){
            throw new BusinessException(ChunkException.FAILED_TO_READ_CHUNK_FILE);
        }finally {
            reentrantReadWriteLock.readLock().unlock();
        }
    }

    private String buildChunkPath(String bucketName,String fileName,Integer chunkNo,String extension){
        // workspace/bucket_filename_chunkNo.extension
        return "%s/%s_%s_%s.%s".formatted(chunkConfig.getWorkSpace(),bucketName,fileName,chunkNo,extension);
    }
}

