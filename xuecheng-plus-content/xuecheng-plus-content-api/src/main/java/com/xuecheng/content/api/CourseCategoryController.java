package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/course-category")
@RequiredArgsConstructor
public class CourseCategoryController {

    private final CourseCategoryService courseCategoryService;

    @GetMapping("/tree-nodes")
    public List<CourseCategoryTreeDto> getCourseCategoryTreeDto() {
        log.info("call getCourseCategoryTreeDto");
        List<CourseCategoryTreeDto> courseCategoryTreeDto = courseCategoryService.getCourseCategoryTreeDto();
//        log.info("getCourseCategoryTreeDto result:{}", courseCategoryTreeDto);
        return courseCategoryTreeDto;
    }
}
