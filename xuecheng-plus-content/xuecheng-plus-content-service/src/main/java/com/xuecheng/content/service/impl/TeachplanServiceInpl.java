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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeachplanServiceInpl implements TeachplanService {
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

        //组合数据
//        teachplans.stream().forEach(
//                teachplan -> {
//                    if(teachplan.getGrade()==1){
//                        TeachplanDto teachplanDto = new TeachplanDto();
//                        BeanUtils.copyProperties(teachplan, teachplanDto);
//                        Optional<TeachplanMedia> teachplanMedia = teachplanMedias.stream().filter(item ->
//                                item.getTeachplanId().equals(teachplan.getId())
//                        ).findFirst();
//                        teachplanMedia.ifPresent(media -> {
//                            TeachplanMedia teachplanMediaItem = new TeachplanMedia();
//                            BeanUtils.copyProperties(media, teachplanMediaItem);
//                            teachplanDto.setTeachplanMedia(teachplanMediaItem);
//                        });
//                        teachplanDtos.add(teachplanDto);
//                    }
//                }
//        );
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
        return teachplanDtos.stream()
                .filter(dto -> dto.getParentid() == 0)
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
}
