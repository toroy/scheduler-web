package com.bigdata.platform.scheduler.web.client.dto;

import com.bigdata.platform.scheduler.common.util.Assert;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class CheckSubscribeDto implements Serializable {
    private static final long serialVersionUID = -7011532706623693393L;

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
     * 订阅人UID
     */
    private String subscriberUid;

    public void checkParams() {
        Assert.nonBlank(this.dataSource, "数据源参数不能为空");
        Assert.nonBlank(this.dbName, "库名参数不能为空");
        Assert.nonBlank(this.tableName, "表名参数不能为空");
        Assert.nonNull(this.subscriberUid, "订阅人UID不能为空");
    }
}
