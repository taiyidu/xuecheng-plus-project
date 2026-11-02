package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    //拿到了熔断的异常信息
    @Override
    public MediaServiceClient create(Throwable cause) {
        return new MediaServiceClient() {
            //发生熔断上传服务调用此方法执行降级逻辑
            @Override
            public String upload(MultipartFile upload, String objName) throws Exception {
                log.error("调用远程的上传文件接口失败,服务触发熔断降级{}",cause.getMessage());
                return "调用远程的上传文件接口失败,服务触发熔断降级";
            }
        };
    }
}
