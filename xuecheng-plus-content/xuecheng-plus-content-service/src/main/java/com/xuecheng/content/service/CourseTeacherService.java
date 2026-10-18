package com.xuecheng.content.service;


import com.xuecheng.content.model.dto.CourseTeacherDto;

import java.util.List;

public interface CourseTeacherService {
    List<CourseTeacherDto> showTeacherInfo(Long courseId);

    CourseTeacherDto saveTeacher(CourseTeacherDto courseTeacherDto, Long companyId);

    void deleteTeacher(Long courseId, Long teacherId, Long companyId);
}
