package com.wbu.controller;

import com.wbu.DTO.FileChunkDTO;
import com.wbu.response.CommonResponse;
import com.wbu.service.FileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    public CommonResponse<?> read(@RequestParam("fileName")String fileName,
                                  @RequestParam("extension")String extension,
                                  @RequestParam("chunkNo")Integer chunkNo,
                                  @RequestParam("bucketName")String bucketName){
        byte[] content=fileService.read(fileName,extension,chunkNo,bucketName);
        return CommonResponse.success(content);
    }
}
