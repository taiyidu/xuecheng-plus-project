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

    /**
     * 展示课程计划树
     * @param courseId
     * @return
     */
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachplanDto> showTeachplan(@PathVariable Long courseId) {
        log.info("show courseId:{}", courseId);
        List<TeachplanDto> teachplanDtoList = teachplanService.showTeachplanList(courseId);
        return teachplanDtoList;
    }

    /**
     * 新增修改接口
     * @param saveTeachplan
     */
    @PostMapping
    public void saveTeachplan(@RequestBody SaveTeachplan saveTeachplan) {
        log.info("saveTeachplan:{}", saveTeachplan);
        teachplanService.saveTeachplan(saveTeachplan);
    }

    /**
     * 删除课程计划接口
     * @param id teachplanId
     */
    @DeleteMapping("/{id}")
    public void deleteTeachplan(@PathVariable Long id) {
        log.info("deleteTeachplan:{}", id);
        teachplanService.deleteTeachplan(id);
    }

    /**
     * 向下移动接口
     * @param id
     */
    @PostMapping("/movedown/{id}")
    public void moveDown(@PathVariable Long id) {
        log.info("moveDown:{}", id);
        teachplanService.moveDown(id);
    }
    @PostMapping("/moveup/{id}")
    public void moveUp(@PathVariable Long id){
        log.info("moveUp:{}", id);
        teachplanService.moveUp(id);
    }
}
