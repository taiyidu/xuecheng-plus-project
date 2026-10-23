package com.xuecheng;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.*;


/**
 * 此文件本来是用来做oss的大文件上传的，但是在测试的时候使用的是minio的思路现在将上传到oss的文件并不能在远程进行合并
 * 所以现在使用aliyunoss的原生的上传方式进行上传
 * 故此文件变成一个废稿，但是在以后如果使用minio的时候或许有帮助
 */
//@SpringBootTest // 加载整个spring容器，和项目启动一样
public class BigFileTest {
    // 成员变量注入，不要写构造注入
//    private AliOssUtil aliOssUtil =null;

    //分块测试
//    @Test
    public void testChunk() throws Exception {
        //源文件
        File sourceFile = new File("D:\\soft\\goole\\video (1).mp4");
        //分块文件存储路径
        String chunkFilePath = "D:\\soft\\goole\\chunk\\";
        //分块文件的大小
        int chunkSize = 1024*1024*1; // 1mb
        //分块文件个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        //使用流向源文件中拿数据，向分块文件中写数据
        //缓冲区
        byte[] bytes = new byte[1024];
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        for (int i=0;i<chunkNum;i++){
            File chunkFile = new File(chunkFilePath + i);
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while((len=raf_r.read(bytes))!=-1){
                raf_rw.write(bytes, 0, len);
                if (chunkFile.length() >= chunkSize){
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }
    //将分块合并
    @Test
    public void testMerge() throws Exception {
        //分块文件
        File chunkFolder = new File("D:\\soft\\goole\\chunk\\");
        //源文件
        File sourceFile = new File("D:\\soft\\goole\\video (1).mp4");
        //合并后的文件
        File mergeFile = new File("D:\\soft\\goole\\merge_video.mp4");
        //取出刚才分的文件
        File[] files = chunkFolder.listFiles();
        List<File> list = Arrays.asList(files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.valueOf(o1.getName()) - Integer.valueOf(o2.getName());
            }
        });

        //向合并文件去写的流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        list.forEach(file -> {
            try {
                RandomAccessFile raf_r = new RandomAccessFile(file, "r");
                int len = -1;
                byte[] bytes = new byte[1024];
                while((len=raf_r.read(bytes))!=-1){
                    raf_rw.write(bytes, 0, len);
                }
                raf_r.close();
                } catch (Exception e) {
                e.printStackTrace();
            }
        });
        raf_rw.close();

        //文件合并完成，对合并的文件进行校验
        FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
        FileInputStream fileInputStream_source = new FileInputStream(sourceFile);
        String merge = DigestUtils.md5Hex(fileInputStream_merge);
        String source = DigestUtils.md5Hex(fileInputStream_source);
        if(merge.equals(source)){
            System.out.println("文件合并成功");
        }
    }
    //将分块上传
    @Test
    public void upoadChunk() throws Exception {
        //将数据存储到阿里云的oss服务上去
        // 你的分片文件夹
        File chunkFolder = new File("D:\\soft\\goole\\chunk\\");
        File[] files = chunkFolder.listFiles();
        List<File> list = Arrays.asList(files);
        // 排序，和之前合并逻辑一样
        list.sort(Comparator.comparingInt(file -> Integer.parseInt(file.getName())));

        for (File chunkFile : list) {
            // 重点：把本地File包装成MockMultipartFile
            FileInputStream fis = new FileInputStream(chunkFile);
            MultipartFile upload = new MockMultipartFile(
                    chunkFile.getName(),
                    chunkFile.getName(),
                    "application/octet-stream",
                    fis
            );
            fis.close();

            // objectKey：media/chunk/分片编号
            String objectKey = "media/chunk/" + chunkFile.getName();
            String url = aliOssUtil.upload(upload, objectKey);
            System.out.println("分片" + chunkFile.getName() + "上传成功，url：" + url);
        }
    }
    //调用接口合并分块
    @Test
    public void testMergeChunks(){

    }
    //批量清理分块内容


}
