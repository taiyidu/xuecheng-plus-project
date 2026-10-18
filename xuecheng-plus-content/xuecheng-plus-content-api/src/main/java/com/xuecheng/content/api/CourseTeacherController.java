package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/courseTeacher")
public class CourseTeacherController {
    private final CourseTeacherService courseTeacherService;

    @GetMapping("/list/{courseId}")
    public List<CourseTeacherDto> show(@PathVariable Long courseId){
        log.info("show teacherDto courseId:{}", courseId);
        return courseTeacherService.showTeacherInfo(courseId);
    }

    /**
     * insert and update teacher info
     * @param courseTeacherDto
     * @return
     */
    @PostMapping
    public CourseTeacherDto saveTeacher(@RequestBody CourseTeacherDto courseTeacherDto){
        log.info("saveTeacher courseTeacherDto:{}", courseTeacherDto);
        Long companyId = 1232141425L;
        return courseTeacherService.saveTeacher(courseTeacherDto, companyId);
    }

    @DeleteMapping("/course/{courseId}/{teacherId}")
    public void deleteTeacher(@PathVariable Long courseId, @PathVariable Long teacherId){
        log.info("deleteTeacher courseId:{}, teacherId:{}", courseId, teacherId);
        Long companyId = 1232141425L;
        courseTeacherService.deleteTeacher(courseId, teacherId,companyId);
    }
}
