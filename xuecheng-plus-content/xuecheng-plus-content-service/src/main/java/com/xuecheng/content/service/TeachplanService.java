package com.xuecheng.content.service;


import com.xuecheng.content.model.dto.SaveTeachplan;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

public interface TeachplanService {
    List<TeachplanDto> showTeachplanList(Long courseId);

    void saveTeachplan(SaveTeachplan saveTeachplan);
}
