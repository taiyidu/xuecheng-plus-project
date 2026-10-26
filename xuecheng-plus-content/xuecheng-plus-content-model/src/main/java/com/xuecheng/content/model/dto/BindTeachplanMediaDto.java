package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BindTeachplanMediaDto {
    private String mediaId;
    private String fileName;
    private Long teachplanId;
}