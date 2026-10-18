package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.CourseBase;

import javax.validation.Valid;

/**
 * 课程基本信息 服务类
 *
 * @author itcast
 * @since 2023-09-06
 */
public interface CourseBaseInfoService {
    /**
     * 课程分页查询
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    CourseBaseInfoDto addCourseBaseInfo(AddCourseDto addCourseDto, Long companyId);

    ShowCourseDto getCourseBaseInfoById(Long courseId);

    ShowCourseDto updateCourseInfo(Long companyId,@Valid UpdateCourseDto updateCourseDto);

    void deleteCourseInfo(Long courseId, Long companyId);
}
