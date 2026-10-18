package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @description 课程信息编辑接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@RestController
@Slf4j
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseBaseInfoController {

    private final CourseBaseInfoService courseBaseInfoService;


    @RequestMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        log.info("pageParams:{}", pageParams);
        log.info("queryCourseParams:{}", queryCourseParamsDto);
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
        return pageResult;
    }
    /**
     * 添加课程
     * @param addCourseDto
     * @return
     */
    @PostMapping
    public CourseBaseInfoDto addCourseBaseInfo(@Valid @RequestBody AddCourseDto addCourseDto) {
        log.info("addCourseBaseInfo:{}", addCourseDto);
        //所属机构的Id 因为后面会从拦截器中获取到id现在暂时获取不到id 所以现在先写死
        // TODO 在本地的线程中获取机构的ID
        Long companyId = 1232141425L;
        return courseBaseInfoService.addCourseBaseInfo(addCourseDto,companyId);
    }

    /**
     * 根据课程id获取课程基本信息
     * @param courseId 课程id
     * @return market and course base info
     */
    @GetMapping("/{courseId}")
    public ShowCourseDto getCourseBaseInfo(@PathVariable Long courseId) {
        log.info("getCourseBaseInfo:{}", courseId);
        return courseBaseInfoService.getCourseBaseInfoById(courseId);
    }

    /**
     * 修改课程信息接口
     */
    @PutMapping
    public ShowCourseDto updateCourseInfo(@Valid @RequestBody UpdateCourseDto updateCourseDto){
        log.info("updateCourseInfo:{}", updateCourseDto);
        // TODO 在本地的线程中获取机构的ID
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseInfo(companyId,updateCourseDto);
    }
    @DeleteMapping("{courseId}")
    public void deleteCourseInfo(@PathVariable Long courseId){
        log.info("deleteCourseInfo:{}", courseId);
        Long companyId = 1232141425L;
        courseBaseInfoService.deleteCourseInfo(courseId,companyId);
    }
}