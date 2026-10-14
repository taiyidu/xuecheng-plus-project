package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    public CourseBaseInfoDto addCourseBaseInfo(@RequestBody AddCourseDto addCourseDto) {
        log.info("addCourseBaseInfo:{}", addCourseDto);
        //所属机构的Id 因为后面会从拦截器中获取到id现在暂时获取不到id 所以现在先写死
        // TODO 在本地的线程中获取机构的ID
        Long companyId = 1111111111L;
        return courseBaseInfoService.addCourseBaseInfo(addCourseDto,companyId);
    }
}