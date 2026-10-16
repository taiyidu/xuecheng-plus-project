package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplan;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/teachplan")
public class TeachplanController {
    private final TeachplanService teachplanService;

    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachplanDto> showTeachplan(@PathVariable Long courseId) {
        log.info("show courseId:{}", courseId);
        List<TeachplanDto> teachplanDtoList = teachplanService.showTeachplanList(courseId);
        return teachplanDtoList;
    }
    @PostMapping
    public void saveTeachplan(@RequestBody SaveTeachplan saveTeachplan) {
        log.info("saveTeachplan:{}", saveTeachplan);
        teachplanService.saveTeachplan(saveTeachplan);
    }
}
