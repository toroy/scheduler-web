package com.clubfactory.platform.scheduler.web.client.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class SimpleSubscribeDto implements Serializable {
    private static final long serialVersionUID = -6418325603889671147L;

    /**
     * 数据源：dbHost / HIVE数据仓库
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

    /**
     * 责任人UID
     */
    private String picUid;

    /**
     * 订阅人uid
     */
    private String subscribeUid;
}
