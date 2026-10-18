package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.*;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    private final CourseBaseMapper courseBaseMapper;

    private final CourseMarketMapper courseMarketMapper;

    private final CourseCategoryMapper courseCategoryMapper;

    private final TeachplanMapper teachplanMapper;

    private final CourseTeacherMapper courseTeacherMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(queryCourseParamsDto != null){
            //模糊查询
            lambdaQueryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
            //审核状态
            lambdaQueryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
            //发布状态
            lambdaQueryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        }
        //分页查询参数对象

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //开始进行分页查询
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, lambdaQueryWrapper);
        //数据列表
        List<CourseBase> records = pageResult.getRecords();
        //总条数
        long total = pageResult.getTotal();
        //返回结果
        return new PageResult<>(records, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    @Transactional
    public CourseBaseInfoDto addCourseBaseInfo(AddCourseDto addCourseDto, Long companyId) {
//        //合法性校验
//        if (StringUtils.isBlank(addCourseDto.getName())) {
////            throw new RuntimeException("课程名称为空");
//            XueChengPlusException.cast("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getMt())) {
////            throw new RuntimeException("课程分类为空");
//            XueChengPlusException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getSt())) {
////            throw new RuntimeException("课程分类为空");
//            XueChengPlusException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getGrade())) {
////            throw new RuntimeException("课程等级为空");
//            XueChengPlusException.cast("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
////            throw new RuntimeException("教育模式为空");
//            XueChengPlusException.cast("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getUsers())) {
////            throw new RuntimeException("适应人群为空");
//            XueChengPlusException.cast("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getCharge())) {
////            throw new RuntimeException("收费规则为空");
//            XueChengPlusException.cast("收费规则为空");
//        }
        //新增CourseBase对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象
        BeanUtils.copyProperties(addCourseDto,courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //插入课程基本信息表
        //这里的id在表中的注解@tableId(type = IdType.AUTO)会将id回填到类中
        int insert = courseBaseMapper.insert(courseBaseNew);
        if(insert<=0){
//            throw new RuntimeException("新增课程基本信息失败");
            XueChengPlusException.cast("新增课程基本信息失败");
        }
        //向课程营销表保存课程营销信息
        //课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(addCourseDto,courseMarketNew);
        courseMarketNew.setId(courseId);
        int i = saveCourseMarket(courseMarketNew);
        if(i<=0){
//            throw new RuntimeException("保存课程营销信息失败");
            XueChengPlusException.cast("保存课程营销信息失败");
        }
        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseId);
    }
    //保存课程营销信息
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
//            throw new RuntimeException("收费规则没有选择");
            XueChengPlusException.cast("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue()<=0){
//                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
            }
        }
        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if(courseMarketObj == null){
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }
    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId){

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }

    @Override
    public ShowCourseDto getCourseBaseInfoById(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        ShowCourseDto showCourseDto = new ShowCourseDto();
        BeanUtils.copyProperties(courseBase, showCourseDto);
        BeanUtils.copyProperties(courseMarket, showCourseDto);
        return showCourseDto;
    }

    @Override
    public ShowCourseDto updateCourseInfo(Long companyId,UpdateCourseDto updateCourseDto) {
        Long courseId = updateCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }
        //封装基本信息的数据 讲数据覆盖
        BeanUtils.copyProperties(updateCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBase);
        BeanUtils.copyProperties(updateCourseDto, courseBase);

        //封装营销信息的数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(updateCourseDto,courseMarket);
        saveCourseMarket(courseMarket);

        //查询课程信息
        CourseBase courseInfo = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarketInfo = courseMarketMapper.selectById(courseId);
        ShowCourseDto courseBaseInfo = new ShowCourseDto();
        BeanUtils.copyProperties(courseInfo, courseBaseInfo);
        BeanUtils.copyProperties(courseMarketInfo, courseBaseInfo);
        return courseBaseInfo;
    }

    @Override
    public void deleteCourseInfo(Long courseId, Long companyId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null || !courseBase.getCompanyId().equals(companyId)){
            throw new RuntimeException("无权限删除课程");
        }
        if(!courseBase.getStatus().equals("203001")){
            throw new RuntimeException("只有未提交才可以删除");
        }
        //delete course info
        courseBaseMapper.deleteById(courseId);
        //delete course market info
        courseMarketMapper.deleteById(courseId);
        //delete teach plan
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(teachplanLambdaQueryWrapper);
        //delete course teacher info
        LambdaQueryWrapper<CourseTeacher> courseTeacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        courseTeacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(courseTeacherLambdaQueryWrapper);
    }
}
