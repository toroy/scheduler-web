package com.zhugeio.platform.meta.client.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TableSimpleDto implements Serializable {

    private static final long serialVersionUID = 7855302219046358764L;

    /**
     * 表名
     */
    private String name;

    /**
     * 库名
     */
    private String dbName;

    /**
     * 数据源, 类型是hive，给值HIVE, 其他为dbHost
     */
    private String dbSource;

    /**
     * 负责人，取单点登录的uid
     */
    private String uid;

    /**
     * 冗余字段
     */
    private Long jobCreateUser;

}
