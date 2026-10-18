package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseTeacherServiceImpl implements CourseTeacherService {
    private final CourseBaseMapper courseBaseMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    @Override
    public List<CourseTeacherDto> showTeacherInfo(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(courseTeachers, CourseTeacherDto.class);
    }

    @Override
    public CourseTeacherDto saveTeacher(CourseTeacherDto courseTeacherDto, Long companyId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacherDto.getCourseId());
        if(courseBase == null || !courseBase.getCompanyId().equals(companyId)){
            throw new RuntimeException("无权限添加教师");
        }
        if(courseTeacherDto.getId()==null){
            //add teacher info
            CourseTeacher courseTeacher = BeanUtil.copyProperties(courseTeacherDto, CourseTeacher.class);
            courseTeacher.setCreateDate(LocalDateTime.now());
            courseTeacherMapper.insert(courseTeacher);
            CourseTeacher courseTeacherItem = courseTeacherMapper.selectById(courseTeacherDto.getId());
            return BeanUtil.copyProperties(courseTeacherItem, CourseTeacherDto.class);
        }else{
            //update teacher info
            CourseTeacher courseTeacher = BeanUtil.copyProperties(courseTeacherDto, CourseTeacher.class);
            courseTeacherMapper.updateById(courseTeacher);
            CourseTeacher courseTeacherItem = courseTeacherMapper.selectById(courseTeacherDto.getId());
            return BeanUtil.copyProperties(courseTeacherItem, CourseTeacherDto.class);
        }

    }
    @Override
    public void deleteTeacher(Long courseId, Long teacherId, Long companyId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null || !courseBase.getCompanyId().equals(companyId)){
            throw new RuntimeException("无权限删除教师");
        }
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        queryWrapper.eq(CourseTeacher::getId, teacherId);
        courseTeacherMapper.delete(queryWrapper);
    }
}

