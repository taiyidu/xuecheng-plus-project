package com.xuecheng.media.api;

import cn.hutool.core.bean.BeanUtil;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @description 媒资文件管理接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class MediaFilesController {
    private final MediaFileService mediaFileService;
    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
        log.info("query media files, pageParams:{}, queryMediaParamsDto:{}", pageParams, queryMediaParamsDto);
        Long companyId = 1232141425L;
        if (pageParams == null || pageParams.getPageNo() == null || pageParams.getPageSize() == null){
            pageParams = new PageParams(1L, 1000L);
        }
        return mediaFileService.queryMediaFiels(companyId,pageParams,queryMediaParamsDto);
    }

    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile upload) throws Exception {
        log.info("upload file interface. file name:{}",upload.getOriginalFilename());
        Long companyId = 1232141425L;
        UploadFileResultDto uploadFileResultDto = mediaFileService.uploadFile(upload,companyId);
        return uploadFileResultDto;
    }
}
