package com.xuecheng.media.api;


import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Mr.M
 * @version 1.0
 * @description 大文件上传接口
 * @date 2022/9/6 11:29
 */
@RequiredArgsConstructor
@RestController
@Slf4j
public class BigFilesController {
    private final MediaFileService mediaFileService;


    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(
    @RequestParam("fileMd5") String fileMd5
    ) throws Exception {
        log.info("checkfile fileMd5:{}", fileMd5);
        return mediaFileService.checkFile(fileMd5);
    }


    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
    @RequestParam("chunk") int chunk) throws Exception {
        log.info("checkchunk fileMd5:{}, chunk:{}", fileMd5, chunk);
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
    @RequestParam("fileMd5") String fileMd5,
    @RequestParam("chunk") int chunk) throws Exception {
        log.info("uploadchunk fileMd5:{}, chunk:{}", fileMd5, chunk);
        return mediaFileService.uploadchunk(file, fileMd5, chunk);
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
    @RequestParam("fileName") String fileName,
    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        log.info("mergechunks fileMd5:{}, fileName:{}, chunkTotal:{}", fileMd5, fileName, chunkTotal);
        return mediaFileService.mergechunks(fileMd5, fileName, chunkTotal);
    }
}