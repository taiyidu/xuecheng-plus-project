package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * 媒资服务feign客户端
 */
@FeignClient(name = "media-api",configuration = {MultipartSupportConfig.class},fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {
//    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PostMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String upload(@RequestPart("filedata") MultipartFile upload,
                                      @RequestParam(value= "objectName",required=false) String objName) throws Exception;
}
