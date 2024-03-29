package com.wbu.services;

import com.wbu.DO.FileChunkMeta;
import com.wbu.DO.MetaFile;
import com.wbu.DTO.FileMeta;
import com.wbu.VO.BucketVO;
import com.wbu.VO.MetaFileVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @auther 11852
 * @create 2023/8/1
 */
public interface FileService {
    String upload(String bucketName,String newFileName, MultipartFile file);

    MetaFile getMeta(String bucketName, String fileName);

    byte[] downloadChunk(FileChunkMeta chunk);

    MetaFileVo meta(FileMeta fileMeta);

    String uploadChunk(String bucketName, String fileName, String md5, Integer chunkNo, MultipartFile file);

    List<BucketVO> files();

    void delete(String bucketName, String fileName);
}
