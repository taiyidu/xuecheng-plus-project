package com.xuecheng.content.api;

import com.xuecheng.content.service.CoursePublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping
public class CoursePublishController {
    private final CoursePublishService coursePublishService;
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        log.info("预览接口，课程ID：{}",courseId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model",coursePublishService.getCoursePreviewInfo(courseId));
        modelAndView.setViewName("course_template");
        log.info("预览接口，返回结果：{}",modelAndView);
        return modelAndView;
    }
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId,courseId);
    }
}
