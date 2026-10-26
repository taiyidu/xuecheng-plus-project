package com.xuecheng.media.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {
    private final MediaFilesMapper mediaFilesMapper;
    private final MinioClient minioClient;
//    private final MediaFileService mediaFileService;
    @Value("${minio.bucket.files}")
    private String bucketName;
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
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
    @Transactional
    public UploadFileResultDto uploadFile(MultipartFile upload, Long companyId) throws Exception {
        // 1. 判断桶是否存在，不存在则创建
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        // 2. 文件名md5防止重名
        String original = upload.getOriginalFilename();
        byte[] fileBytes = upload.getBytes();
        String objName = DigestUtils.md5Hex(fileBytes);



        // 4. 计算文件的 md5 作为文件id
        String fileMd5 = DigestUtils.md5Hex(fileBytes);

        // 5. 提取并转小写后缀
        String extension = FilenameUtils.getExtension(original);

        // 6. 核心修复：先获取文件类型描述（"课程视频"、"课程图片" 或 "other"）
        String fileTypeDesc = extracted(extension);

        // 7. 根据黑马项目的字典表规定转换为对应编码：课程视频("001002")，课程图片("001001")，其他("001003")
        String fileTypeCode = "001003";
        if ("课程视频".equals(fileTypeDesc)) {
            fileTypeCode = "001002";
        } else if ("课程图片".equals(fileTypeDesc)) {
            fileTypeCode = "001001";
        }

        // 8. 构建实体类并保存到数据库
        MediaFiles mediaFiles = MediaFiles.builder()
                .id(fileMd5)
                .companyId(companyId)
                .filename(original)
                .fileType(fileTypeCode)               // 正确的字典编码
                .tags(extension)                      // 标签存文件后缀（如 mp4, jpg）
                .bucket(bucketName)
                .filePath(objName)
                .fileId(fileMd5)
                .url(endpoint + "/" + bucketName + "/" + objName) // 确保桶已设为 Public/Download 权限
                .createDate(LocalDateTime.now())
                .status("1")
                .auditStatus("002003")
                .fileSize(upload.getSize())
                .build();


        try {
            //将数据同步到数据库
            mediaFilesMapper.insert(mediaFiles);
            // 3. 上传文件到 MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objName)
                    .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                    .contentType(upload.getContentType())
                    .build());
        } catch (DuplicateKeyException e) {
            // 专门捕获md5已存在（秒传场景）
            log.warn("文件md5已存在，无需重复上传,md5:{}",fileMd5);
            // 抛业务异常，前端拿到提示
            throw new XueChengPlusException("该文件已经上传过了，直接使用已有文件");
        } catch (Exception e) {
            log.error("上传失败",e);
            throw new XueChengPlusException("文件上传失败");
        }


        // 9. 将 UploadFileResultDto 返回给前端
        return BeanUtil.copyProperties(mediaFiles, UploadFileResultDto.class);
    }

    /**
     * 辅助方法：根据后缀判断文件大类
     */
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

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles != null){
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            //如果数据库存在再查询minio
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            //远程查询获得一个流对象
            try{
                GetObjectResponse inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null){
                    //文件已存在
                    log.error("视频文件已存在");
                    return RestResponse.success(true);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //分块的存储路径是md5值的前两位
        //根据md5得到分块文件路径
        String chunkFileFoldPath = getChunkPath(fileMd5);
        //先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles != null){
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            //如果数据库存在再查询minio
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket_video)
                    .object(chunkFileFoldPath + chunkIndex)
                    .build();
            //远程查询获得一个流对象
            try{
                GetObjectResponse inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null){
                    //文件已存在
                    return RestResponse.success(true);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    /**
     * 这个是视频分片的上传的接口的方法，会将分片的前两个的字母作为文件夹名字，这个也算一个算法吧
     * 在查找分片的接口中会加速分片的查找
     * @param fileMd5 文件的md5值
     * @return 分块文件存储路径
     */
    @NotNull
    private static String getChunkPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 这个是上传分片的方法，所以没有上传的sql记录，但是应该在合并的时候会有上传的记录生成
     * @param file 分块文件
     * @param fileMd5 文件的md5值
     * @param chunk 分块文件的序号
     * @param file 分块文件
     * @param fileMd5 文件的md5值
     * @param chunk 分块文件的序号
     * @return 前端的返回值
     */
    @Override
    public RestResponse uploadchunk(MultipartFile file, String fileMd5, int chunk) {
        // 使用传入的 fileMd5 获取分块文件存储路径（与 checkChunk 保持一致）
        String chunkFilePath = getChunkPath(fileMd5) + chunk;
        // 获取mimeType
        String mimeType = getMimeType(null);
        // 将分块文件上传到minio
        boolean b = addMediaFilesToMinIO(file, mimeType, bucket_video, chunkFilePath);
        if (!b) {
            return RestResponse.validfail("上传分块文件失败");
        }
        return RestResponse.success(true);
    }
    public boolean addMediaFilesToMinIO(MultipartFile file, String mimeType, String bucket, String objectName) {
        try {
            // 判断桶是否存在，不存在则创建（检查传入的 bucket 参数）
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(mimeType)
                    .build());
            log.info("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
            return true;
        } catch (Exception e) {
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
            XueChengPlusException.cast("上传文件到文件系统失败");
        }
        return false;
    }
    private String getMimeType(String extension){
        if(extension==null)
            extension = "";
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }
    @Override
    public RestResponse mergechunks(String fileMd5, String fileName, int chunkCount, Long companyId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String bucket = bucket_video;
        String finalObjKey = "video/" + fileName;

        List<ComposeSource> sourceList = new ArrayList<>();
        // 顺序严格：0,1,2,3... 顺序错视频直接损坏
        for (int i = 0; i < chunkCount; i++) {
            sourceList.add(ComposeSource.builder()
                    .bucket(bucket)
                    .object(getChunkPath(fileMd5) + i) // 和分片上传时存的路径一致
                    .build());
        }

        ComposeObjectArgs args = ComposeObjectArgs.builder()
                .bucket(bucket)
                .object(finalObjKey)
                .sources(sourceList)
                .build();

        // 远程执行合并，服务端内部拼接
        minioClient.composeObject(args);



        // 提取并转小写后缀
        String extension = FilenameUtils.getExtension(fileName);

        // 核心修复：先获取文件类型描述（"课程视频"、"课程图片" 或 "other"）
        String fileTypeDesc = extracted(extension);

        // 根据黑马项目的字典表规定转换为对应编码：课程视频("001002")，课程图片("001001")，其他("001003")
        String fileTypeCode = "001003";
        if ("课程视频".equals(fileTypeDesc)) {
            fileTypeCode = "001002";
        } else if ("课程图片".equals(fileTypeDesc)) {
            fileTypeCode = "001001";
        }
        //文件大小
        StatObjectResponse stat = null;
        try {
            stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(finalObjKey)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 8. 构建实体类并保存到数据库
        MediaFiles mediaFiles = MediaFiles.builder()
                .id(fileMd5)
                .companyId(companyId)
                .filename(fileName)
                .fileType(fileTypeCode)               // 正确的字典编码
                .tags(extension)                      // 标签存文件后缀（如 mp4, jpg）
                .bucket(bucket_video)
                .filePath(finalObjKey)
                .fileId(fileMd5)
                .url(endpoint + "/" + bucket + "/" + finalObjKey) // 确保桶已设为 Public/Download 权限
                .createDate(LocalDateTime.now())
                .status("1")
                .auditStatus("002003")
                .fileSize(stat.size())
                .build();
        try {
            mediaFilesMapper.insert(mediaFiles);
        } catch (DuplicateKeyException e) {
            // 专门捕获md5已存在（秒传场景）
            log.warn("文件md5已存在，无需重复上传,md5:{}",fileMd5);
            // 抛业务异常，前端拿到提示
            throw new XueChengPlusException("该文件已经上传过了，直接使用已有文件");
        } catch (Exception e) {
            log.error("insert file to db fail",e);
            throw new XueChengPlusException("insert file to db fail");
        }
        //清理分块文件
        clearChunkFiles(getChunkPath(fileMd5), chunkCount);

        return RestResponse.success(true);
    }

    private void clearChunkFiles(String chunkPath, int chunkCount) {
        Iterable<DeleteObject> objects = Stream
                .iterate(0,i->++i)
                .limit(chunkCount)
                .map(i->new DeleteObject(chunkPath+i))
                .collect(Collectors.toList());
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        for (Result<DeleteError> result : results) {
            try {
                DeleteError err = result.get();
                log.error("分片删除失败 key={}, 原因={}", err.objectName(), err.message());
            } catch (Exception ignored) {
                // get抛异常 = 删除成功，直接跳过
            }
        }
    }
}
