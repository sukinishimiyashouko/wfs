package com.wbu.controller;

import com.wbu.DO.FileChunkMeta;
import com.wbu.DO.MetaFile;
import com.wbu.DTO.CompleteChunkFileDTO;
import com.wbu.DTO.FileMeta;
import com.wbu.VO.BucketVO;
import com.wbu.response.CommonResponse;
import com.wbu.services.MetaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @auther 11852
 * @create 2023/7/31
 */
@RestController
@Validated
@RequestMapping("/meta")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    /**
     * 获取meta元数据 并返回给数据库中
     * @return 元数据
     */
    @PostMapping("/generate")
    public CommonResponse<MetaFile> generate(@Validated @RequestBody FileMeta fileMeta){
        MetaFile metaFile = metaService.generate(fileMeta);
        return CommonResponse.success(metaFile);
    }

    /**
     * 供下载使用
     * 从数据库中获取meta元数据
     * @return 元数据
     */
    @GetMapping("/info")
    public CommonResponse<?> info(@RequestParam("bucketName")@NotBlank(message = "存储桶名不得为空") String bucketName,
                                  @RequestParam("fileName") @NotBlank(message = "文件名不得为空") String fileName){
        MetaFile metaFile = metaService.meta(bucketName,fileName);
        return CommonResponse.success(metaFile);
    }

    /**
     * 分片上传完成
     * @return void
     */
    @PostMapping("/chunk/complete")
    public CommonResponse<Void> chunkComplete(@Validated @RequestBody CompleteChunkFileDTO completeChunkFileDTO){
        metaService.completeChunk(completeChunkFileDTO);
        return CommonResponse.success();
    }

    /**
     * 获取文件分片信息
     * @param bucketName
     * @param fileName
     * @param chunkNo
     * @return
     */
    @GetMapping("/chunk/info")
    public CommonResponse<?> chunkInfo(@RequestParam("bucketName") @NotBlank(message = "存储桶名不得为空") String bucketName,
                                       @RequestParam("fileName") @NotBlank(message = "文件名不得为空") String fileName,
                                       @RequestParam("chunkNo") @NotNull(message = "分片序号不得为空") Integer chunkNo) {
        List<FileChunkMeta> chunks = metaService.chunkInfo(bucketName, fileName, chunkNo);
        return CommonResponse.success(chunks);
    }

    @GetMapping("/files")
    public CommonResponse<List<BucketVO>> files(){
        List<BucketVO> files = metaService.files();
        return CommonResponse.success(files);
    }

    @DeleteMapping("{bucketName}/{fileName}")
    public CommonResponse<?> delete(@PathVariable @NotBlank(message = "存储桶名不得为空") String bucketName,
                                    @PathVariable @NotBlank(message = "文件名不得为空") String fileName){
        metaService.delete(bucketName,fileName);
        return CommonResponse.success();
    }
}
