package com.bigdata.platform.scheduler.web.server.dqc.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DqcTableDto implements Serializable  {

    private static final long serialVersionUID = -356980515514559711L;
    /**
     * 库名
     */
    private String dbName;

    /**
     * 表名
     */
    private String tableName;
}
