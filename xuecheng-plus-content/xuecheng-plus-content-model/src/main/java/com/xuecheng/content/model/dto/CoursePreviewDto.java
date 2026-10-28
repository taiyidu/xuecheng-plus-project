package com.xuecheng.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 用于课程预览的模型类
 */
@Data
public class CoursePreviewDto {
    //课程的基本信息 营销信息
    private ShowCourseDto courseBase;
    //课程计划信息
    private List<TeachplanDto> teachplans;
    //课程师资信息

}
