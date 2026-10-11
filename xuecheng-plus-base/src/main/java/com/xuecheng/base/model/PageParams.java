package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 分页查询的参数
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {
    private Long pageNo;
    private Long pageSize;
}
