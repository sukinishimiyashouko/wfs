package com.wbu.controller;

import com.wbu.DTO.FileChunkDTO;
import com.wbu.response.CommonResponse;
import com.wbu.service.FileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @auther 11852
 * @create 2023/8/1
 */
@Validated
@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/write")
    public CommonResponse<String> write(@Valid @RequestBody FileChunkDTO fileChunkDTO){
        String fileMd5 = fileService.write(fileChunkDTO);
        return CommonResponse.success(fileMd5);
    }

    @GetMapping("/read")
    public CommonResponse<?> read(@RequestParam("fileName") @NotBlank(message = "文件名不得为空") String fileName,
                                  @RequestParam("extension") String extension,
                                  @RequestParam("chunkNo") @NotNull(message = "文件分片序号不得为空") Integer chunkNo,
                                  @RequestParam("bucketName") @NotBlank(message = "存储桶名不得为空") String bucketName){
        byte[] content=fileService.read(fileName,extension,chunkNo,bucketName);
        return CommonResponse.success(content);
    }
}
