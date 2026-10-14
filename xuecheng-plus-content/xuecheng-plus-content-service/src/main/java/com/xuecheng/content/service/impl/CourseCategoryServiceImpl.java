package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseCategoryServiceImpl implements CourseCategoryService {
    private final CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> getCourseCategoryTreeDto() {
        List<CourseCategory> allCourseCategoryList = courseCategoryMapper.selectList(null);
        List<CourseCategoryTreeDto> dtoList = BeanUtil.copyToList(allCourseCategoryList, CourseCategoryTreeDto.class);
        // 3. 构建map：key=id，value=当前分类DTO，方便快速查找父节点
        Map<String, CourseCategoryTreeDto> nodeMap = dtoList.stream()
                .collect(Collectors.toMap(CourseCategoryTreeDto::getId, dto -> dto));

        // 4. 存放根节点集合（parentid="1"）
        List<CourseCategoryTreeDto> rootNodes = new ArrayList<>();

        // 5. 遍历所有节点，绑定父子关系
        for (CourseCategoryTreeDto dto : dtoList) {
            String parentId = dto.getParentid();
            // 如果父id是根标记，加入根集合
            if ("1".equals(parentId)) {
                rootNodes.add(dto);
            } else {
                // 找到父节点
                CourseCategoryTreeDto parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    // 父节点的childrenTreeNodes初始化
                    if (parentNode.getChildrenTreeNodes() == null) {
                        parentNode.setChildrenTreeNodes(new ArrayList<>());
                    }
                    // 当前节点作为子节点放入父节点
                    parentNode.getChildrenTreeNodes().add(dto);
                }
            }
        }
        log.info("rootNodes:{}", rootNodes);
        return rootNodes;
    }
}
