package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

@SpringBootTest
//@RequiredArgsConstructor
public class FeignUploadTest {
    @Autowired
    private MediaServiceClient mediaServiceClient;
    @Test
    public void testFeignUpload() throws Exception {
        //将file转成multipartfile
        File file = new File("D:\\BaiduNetdiskDownload\\xuechenghtmltempupload\\1.html");
//        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                file.getName(),
                "text/html",
                Files.readAllBytes(file.toPath())
        );
        mediaServiceClient.upload(multipartFile,"course/1.html");
    }
}
