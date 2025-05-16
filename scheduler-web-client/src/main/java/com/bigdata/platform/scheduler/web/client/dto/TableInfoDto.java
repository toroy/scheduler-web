package com.bigdata.platform.scheduler.web.client.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class TableInfoDto implements Serializable {
    private static final long serialVersionUID = -8596544387331842797L;

    /**
     * 责任人uid
     */
    private String picUid;

    /**
     * 数据源： HIVE/dbHost
     */
    private String dataSource;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 表名称
     */
    private String tableName;

}
