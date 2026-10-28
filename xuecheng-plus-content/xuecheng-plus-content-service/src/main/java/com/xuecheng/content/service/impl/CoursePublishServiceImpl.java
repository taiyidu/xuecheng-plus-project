package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.ShowCourseDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程发布相关接口实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CoursePublishServiceImpl implements CoursePublishService {
    private final CourseBaseInfoService courseBaseInfoService;
    private final TeachplanService teachplanService;
    private final CourseMarketMapper courseMarketMapper;
    private final CoursePublishPreMapper coursePublishPreMapper;
    private final CourseBaseMapper courseBaseMapper;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //查询课程的基本信息以及营销信息
        ShowCourseDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfoById(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanService.showTeachplanList(courseId));
        //返回组装的信息
        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        ShowCourseDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfoById(courseId);
        //设置机构的校验
        if (!courseBaseInfo.getCompanyId().equals(companyId)){
            throw new XueChengPlusException("不是本机构无权限操作");
        }
        if(courseBaseInfo == null){
            throw new XueChengPlusException("课程不存在");
        }
        //如果课程的审核状态为已提交则不允许提交
        String auditStatus = courseBaseInfo.getAuditStatus();
        if (auditStatus.equals("202003")){
            throw new XueChengPlusException("课程已提交，请等待审核");
        }
        //课程的图片,计划信息没有填写则也不允许提交
        String pic = courseBaseInfo.getPic();
        if (pic.isEmpty()){
            throw new XueChengPlusException("请上传课程图片");
        }
        List<TeachplanDto> teachplanDtos = teachplanService.showTeachplanList(courseId);
        if (teachplanDtos ==null || teachplanDtos.isEmpty()){
            throw new XueChengPlusException("请填写课程计划");
        }
        //查询课程的基本信息，营销信息，计划信息等插入到课程的预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        log.info("courseBaseInfo:{}", courseBaseInfo);
        log.info("coursePublishPre:{}", coursePublishPre);

        //设置机构id
        coursePublishPre.setCompanyId(companyId);
        //更新课程基本信息提交状态为已提交
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        coursePublishPre.setStatus("202003");
        //计划信息
        String teachplanDtosJson = JSON.toJSONString(teachplanDtos);
        coursePublishPre.setTeachplan(teachplanDtosJson);
        //提交时间
        coursePublishPre.setAuditDate(LocalDateTime.now());
        //插入
        CoursePublishPre coursePublishPreInfo = coursePublishPreMapper.selectById(coursePublishPre.getId());
        if (coursePublishPreInfo == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //更新课程基本信息表
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }
}
