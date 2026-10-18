package com.xuecheng.content.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplan;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeachplanServiceImpl implements TeachplanService {
    private final TeachplanMapper teachplanMapper;
    private final TeachplanMediaMapper teachplanMediaMapper;
    @Override
    public List<TeachplanDto> showTeachplanList(Long courseId) {
        //查询Teachplan
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        List<TeachplanDto> teachplanDtos = BeanUtil.copyToList(teachplans, TeachplanDto.class);
        //------------------------------------------------------
        Map<Long, TeachplanDto> teachplanDtoHashMap = new HashMap<>();
        teachplanDtos.forEach(teachplanDto -> teachplanDtoHashMap.put(teachplanDto.getId(), teachplanDto));
        //------------------------------------------------------

        //查询teachplan Media
        LambdaQueryWrapper<TeachplanMedia> teachmediaQueryWrapper = new LambdaQueryWrapper<>();
        teachmediaQueryWrapper.eq(TeachplanMedia::getCourseId,courseId);
        List<TeachplanMedia> teachplanMedias = teachplanMediaMapper.selectList(teachmediaQueryWrapper);
        //------------------------------------------------------
        Map<Long, TeachplanMedia> teachplanMediaHashMap = new HashMap<>();
        teachplanMedias.forEach(teachplanMedia->teachplanMediaHashMap.put(teachplanMedia.getTeachplanId(), teachplanMedia));
        //------------------------------------------------------
        teachplanDtos.forEach(teachplanDto->{
            //存media
            teachplanDto.setTeachplanMedia(teachplanMediaHashMap.get(teachplanDto.getId()));
            //找到父节点
            Long parentid = teachplanDto.getParentid();
            if(parentid>0){
                TeachplanDto teachplanDtoItem = teachplanDtoHashMap.get(parentid);
                if(teachplanDtoItem.getTeachPlanTreeNodes()==null){
                    teachplanDtoItem.setTeachPlanTreeNodes(new ArrayList<>());
                }
                teachplanDtoItem.getTeachPlanTreeNodes().add(teachplanDto);
            }

        });
        teachplanDtos.forEach(teachplanDto->{
            List<TeachplanDto> teachPlanTreeNodes = teachplanDto.getTeachPlanTreeNodes();
            if(teachPlanTreeNodes!=null){
                List<TeachplanDto> child = teachPlanTreeNodes
                        .stream()
                        .sorted(Comparator.comparing(TeachplanDto::getOrderby))
                        .collect(Collectors.toList());
                teachplanDto.setTeachPlanTreeNodes(child);
            }
        });

        return teachplanDtos.stream()
                .filter(dto -> dto.getParentid() == 0)
                .sorted(Comparator.comparing(TeachplanDto::getOrderby))
                .collect(Collectors.toList());
    }

    @Override
    public void saveTeachplan(SaveTeachplan saveTeachplan) {
        Long TeachplanId = saveTeachplan.getId();
        if(TeachplanId!=null){
            //修改
            Teachplan teachplan = teachplanMapper.selectById(saveTeachplan.getId());
            BeanUtil.copyProperties(saveTeachplan, teachplan);
            teachplanMapper.updateById(teachplan);
        }else{
            //新增
            //取出同父同级别的课程计划数量
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,saveTeachplan.getParentid());
            queryWrapper.eq(Teachplan::getCourseId,saveTeachplan.getCourseId());
            Integer count = teachplanMapper.selectCount(queryWrapper);
            Teachplan teachplan = new Teachplan();
            BeanUtil.copyProperties(saveTeachplan, teachplan);
            teachplan.setOrderby(count + 1);
            teachplanMapper.insert(teachplan);
        }
    }

    @Transactional
    @Override
    public void deleteTeachplan(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan.getGrade()==1){
            //查第一级章节如果有子节点就不删除抛出错误，没有就直接删除
            LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
            teachplanLambdaQueryWrapper.eq(Teachplan::getGrade,2);
            teachplanLambdaQueryWrapper.eq(Teachplan::getParentid,teachplan.getId());
            if(teachplanMapper.selectCount(teachplanLambdaQueryWrapper)>0){
                throw new XueChengPlusException("请先删除子节点");
            }else{
                teachplanMapper.deleteById(id);
            }
        }else{
            //查出第二节的时候同时删除media表
            LambdaQueryWrapper<TeachplanMedia> mediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
            mediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId,teachplan.getId());
            //健壮性判断
            if(teachplanMediaMapper.selectCount(mediaLambdaQueryWrapper) > 0){
                teachplanMediaMapper.delete(mediaLambdaQueryWrapper);
            }
            teachplanMapper.deleteById(id);
        }
    }

    @Override
    @Transactional
    public void moveDown(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if(teachplan==null){//健壮性判断
            throw new XueChengPlusException("移动的节点不存在");
        }
        //同一个父节点下的同级章节
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getParentid,teachplan.getParentid());
        List<Teachplan> teachplanList = teachplanMapper.selectList(teachplanLambdaQueryWrapper);
        //找到同级下的紧邻的下一条
        Teachplan teachplanNext = teachplanList.stream()
                .filter(teachplanItem->teachplanItem.getOrderby() > teachplan.getOrderby())
                .sorted(Comparator.comparing(Teachplan::getOrderby)) // orderby升序，小的放前面
                .findFirst()
                .orElse(null);
        if(teachplanNext==null){
            throw new XueChengPlusException("已经是最后了");
        }
        //交换两个章节的orderby
        Integer temp = teachplan.getOrderby();
        teachplan.setOrderby(teachplanNext.getOrderby());
        teachplanNext.setOrderby(temp);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplanNext);
    }

    @Override
    @Transactional
    public void moveUp(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if(teachplan==null){//健壮性判断
            throw new XueChengPlusException("移动的节点不存在");
        }
        //同一个父节点下的同级章节
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getParentid,teachplan.getParentid());
        List<Teachplan> teachplanList = teachplanMapper.selectList(teachplanLambdaQueryWrapper);
        //找到同级下的紧邻的下一条
        Teachplan teachplanNext = teachplanList.stream()
                .filter(teachplanItem->teachplanItem.getOrderby() < teachplan.getOrderby())
                .sorted(Comparator.comparing(Teachplan::getOrderby).reversed()) // orderby升序，小的放前面
                .findFirst()
                .orElse(null);
        if(teachplanNext==null){
            throw new XueChengPlusException("已经是最前了");
        }
        //交换两个章节的orderby
        Integer temp = teachplan.getOrderby();
        teachplan.setOrderby(teachplanNext.getOrderby());
        teachplanNext.setOrderby(temp);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(teachplanNext);
    }
}
