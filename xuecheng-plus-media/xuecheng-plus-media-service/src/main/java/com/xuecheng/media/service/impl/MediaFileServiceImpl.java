package com.xuecheng.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.util.AliOssProperties;
import com.xuecheng.media.util.AliOssUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl implements MediaFileService {
    private final MediaFilesMapper mediaFilesMapper;

    private final AliOssUtil aliOssUtil;

    private final AliOssProperties aliOssProperties;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;
    }

    @Override
    public UploadFileResultDto uploadFile(MultipartFile upload,Long companyId) throws Exception {
        //将数据存储到阿里云的oss服务上去
        // 生成唯一objectKey，避免重名：比如 media/20261020/uuid.mp4
        String suffix = FilenameUtils.getExtension(upload.getOriginalFilename());
        String objectKey = "media/" + DateUtil.today() + "/" + UUID.randomUUID() + "." + suffix;
        String url = aliOssUtil.upload(upload, objectKey);
        //将数据备份到数据库上去
        // 对MultipartFile流计算md5
        String fileMd5 = DigestUtils.md5Hex(upload.getInputStream());
        // 提取后缀、转小写

        MediaFiles mediaFiles = MediaFiles.builder()
                .id(fileMd5)
                .companyId(companyId)
                .filename(upload.getOriginalFilename())
                .fileType(extracted(suffix).equals("课程视频") ? "001002" : "001001")
                .tags(extracted(suffix))
                .bucket(aliOssProperties.getBucketName())
                .filePath(objectKey)
                .fileId(fileMd5)
                .url(url)
                .createDate(LocalDateTime.now())
                .status("1")
                .auditStatus("002003")
                .fileSize(upload.getSize())
                .build();
        mediaFilesMapper.insert(mediaFiles);
        //将需求的UploadFileResultDto返回到前端
        return BeanUtil.copyProperties(mediaFiles, UploadFileResultDto.class);
    }

    private static String extracted(String suffix) {
        suffix = suffix == null ? "" : suffix.toLowerCase();

        String fileType = "other";
        List<String> videoSuffixList = Arrays.asList(
                "mp4", "mov", "avi", "mkv", "flv",
                "wmv", "rm", "rmvb", "webm", "m4v",
                "3gp", "mpeg", "mpg"
        );
        List<String> imgSuffixList = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

        if (videoSuffixList.contains(suffix)) {
            fileType = "课程视频";
        } else if (imgSuffixList.contains(suffix)) {
            fileType = "课程图片";
        }
        return fileType;
    }
}
