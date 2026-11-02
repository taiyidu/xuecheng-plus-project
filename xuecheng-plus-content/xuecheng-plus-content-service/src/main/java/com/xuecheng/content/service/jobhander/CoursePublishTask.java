package com.xuecheng.content.service.jobhander;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {
    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
        //调用抽象类方法调用方法
        process(shardIndex,shardTotal,"course_publish",10,60);
    }

    //执行课程发布逻辑的方法
    @Override
    public boolean execute(MqMessage mqMessage) {
        //拿到课程id
        Long courseId = Long.valueOf(mqMessage.getBusinessKey1());
        //课程静态化上传到 minio
        generateCourseHtml(mqMessage,courseId);
        //向es写索引数据
        saveCourseIndex(mqMessage,courseId);
        //向redis写缓存

        return true;
    }
    //保存课程索引信息 第一个阶段的任务
    private void generateCourseHtml(MqMessage mqMessage,Long courseId){
        log.info("生成课程静态化(upload for minio)，课程id：{}",courseId);
        MqMessageService mqMessageService = this.getMqMessageService();

        //做一下幂等性

        //查询数据库取出该阶段的执行状态
        Long taskId = mqMessage.getId();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0){
            log.info("课程静态化已生成无需处理，课程id：{}",courseId);
            return;
        }
        //开始进行课程静态化

        //任务处理完成写任务状态为完成
        mqMessageService.completedStageOne(taskId);
    }
    //保存课程信息第二个阶段的任务
    public void saveCourseIndex(MqMessage mqMessage,Long courseId){
        log.info("向es写索引数据，课程id：{}",courseId);
        //任务id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //取出第二个状态
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0){
            log.info("向es写数据任务已生成无需处理，课程id：{}",courseId);
            return;
        }
        //任务幂等性判断处理

        //查询课程信息，调用搜索服务添加索引 ...

        //完成阶段的任务
        mqMessageService.completedStageTwo(taskId);
    }
}
