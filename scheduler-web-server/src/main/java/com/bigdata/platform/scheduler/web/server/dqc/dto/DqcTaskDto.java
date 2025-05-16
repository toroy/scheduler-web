package com.bigdata.platform.scheduler.web.server.dqc.dto;

import com.bigdata.platform.scheduler.common.bean.Pager;
import com.bigdata.platform.scheduler.web.server.dqc.enums.DqcTaskStatusEnum;
import lombok.Data;

@Data
public class DqcTaskDto extends Pager {

    /**
     * 数据库名
     */
    private String dbName;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 实例id
     */
    private Long targetTaskId;

    /**
     * 状态
     */
    private DqcTaskStatusEnum status;

}
