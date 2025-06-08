package com.zhugeio.platform.meta.client.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class ColumnDto implements Serializable {

    /**
     * 字段名
     */
    private String name;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 字段备注
     */
    private String comment;

    /**
     * 是否分区字段
     */
    private Boolean isPartition;

}
