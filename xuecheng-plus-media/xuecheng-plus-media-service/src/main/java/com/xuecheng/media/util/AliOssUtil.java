package com.xuecheng.media.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AliOssUtil {
    @Autowired
    private AliOssProperties ossProp;

    // 上传MultipartFile流
    public String upload(MultipartFile file, String objectKey) throws Exception {
        OSS ossClient = new OSSClientBuilder()
                .build(ossProp.getEndpoint(), ossProp.getAccessKeyId(), ossProp.getAccessKeySecret());
        try {
            ossClient.putObject(ossProp.getBucketName(), objectKey, file.getInputStream());
            // 返回文件完整访问地址（私有桶不能直接浏览器打开，后面要签名url）
            return "https://" + ossProp.getBucketName() + "." + ossProp.getEndpoint() + "/" + objectKey;
        } finally {
            ossClient.shutdown();
        }
    }

    // 删除文件
    public void delete(String objectKey){
        OSS ossClient = new OSSClientBuilder()
                .build(ossProp.getEndpoint(), ossProp.getAccessKeyId(), ossProp.getAccessKeySecret());
        try {
            ossClient.deleteObject(ossProp.getBucketName(), objectKey);
        }finally {
            ossClient.shutdown();
        }
    }
}
