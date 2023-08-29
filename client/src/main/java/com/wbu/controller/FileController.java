package com.wbu.controller;

import com.wbu.DO.FileChunkMeta;
import com.wbu.DO.MetaFile;
import com.wbu.DTO.FileMeta;
import com.wbu.VO.BucketVO;
import com.wbu.VO.MetaFileVo;
import com.wbu.errors.BusinessException;
import com.wbu.errors.EnumClientException;
import com.wbu.response.CommonResponse;
import com.wbu.services.FileService;
import com.wbu.util.Md5Util;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Validated
@RestController
@RequestMapping("/")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 小文件上传
     * @param bucketName 存储桶名称
     * @param file 文件
     * @return 文件路径
     */
    @PostMapping("/upload")
    public CommonResponse<String> upload(@RequestParam("bucket") @NotBlank(message = "存储桶名不得为空") String bucketName,
                                         @RequestParam("file") @NotNull(message = "文件不得为空") MultipartFile file){
        String fileUrl = fileService.upload(bucketName,file);
        return CommonResponse.success(fileUrl);
    }


    /** 大文件分片上传
     * 生成meta信息
     * @param fileMeta
     * @return metaFile
     */
    @PostMapping("/meta")
    public CommonResponse<?> meta(@RequestBody @Valid FileMeta fileMeta){
        MetaFileVo metaFileVo = fileService.meta(fileMeta);
        return CommonResponse.success(metaFileVo);
    }

    /**
     * 分片文件上传
     * @param bucketName 完整文件访问路径
     * @param fileName fileName
     * @param fileName 完整的文件路径
     * @param md5   分片的md5
     * @param chunkNo   分片序号
     * @param file  分片文件
     * @return MD5
     */
    @PostMapping("/chunk/upload")
    public CommonResponse<?> chunkUpload(@RequestParam("bucketName") @NotBlank(message = "存储桶名不得为空") String bucketName,
                                         @RequestParam("fileName") @NotBlank(message = "文件名不得为空") String fileName,
                                         @RequestParam("md5") @NotBlank(message = "文件md5不得为空") String md5,
                                         @RequestParam("chunkNo") @Min(value = 0,message = "文件序号不得为空") Integer chunkNo,
                                         @RequestParam("file") @NotNull(message = "文件不得为空") MultipartFile file){
        String fileMd5 = fileService.uploadChunk(bucketName,fileName,md5,chunkNo,file);
        return CommonResponse.success(fileMd5);
    }

    /**
     * 文件下载
     * @param response response
     * @param bucketName 存储桶名
     * @param fileName 文件名
     * @return
     */
    @GetMapping("{bucketName}/{fileName}")
    public void download(HttpServletResponse response
                                    ,@PathVariable @NotBlank(message = "存储桶名不得为空") String bucketName
                                    ,@PathVariable @NotBlank(message = "文件名不得为空") String fileName){
        MetaFile metaFile = fileService.getMeta(bucketName,fileName);

        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(metaFile.getFileSize().intValue());
        response.setHeader("Content-DisPosition","attachment;fileName="+metaFile.getFileName()+"."+metaFile.getExtension());

        //下载每一个分片信息
        for (FileChunkMeta chunk : metaFile.getChunks()) {
            byte[] content = fileService.downloadChunk(chunk);
            String md5 = Md5Util.getMd5(content);
            if (!md5.equals(chunk.getChunkMd5())){
                throw new BusinessException(EnumClientException.CHUNK_FILE_INCOMPLETE);
            }
            try{
                response.getOutputStream().write(content);
            }catch (Exception e){
                throw new BusinessException(EnumClientException.FAILED_TO_DOWNLOAD_FILE);
            }
        }
    }

    @GetMapping("/files")
    public CommonResponse<?> files(){
        List<BucketVO> files = fileService.files();
        return CommonResponse.success(files);
    }

    /**
     * 删除
     * @param bucketName
     * @param fileName
     * @return
     */
    @DeleteMapping("{bucketName}/{fileName}")
    public CommonResponse<?> delete(@PathVariable @NotBlank(message = "存储桶名不得为空") String bucketName,
                                    @PathVariable @NotBlank(message = "文件名不得为空") String fileName){
        fileService.delete(bucketName,fileName);
        return CommonResponse.success();
    }
}
