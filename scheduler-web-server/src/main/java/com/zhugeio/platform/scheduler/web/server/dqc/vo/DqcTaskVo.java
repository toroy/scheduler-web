package com.zhugeio.platform.scheduler.web.server.dqc.vo;

import com.zhugeio.platform.scheduler.web.server.dqc.enums.DqcTaskStatusEnum;
import lombok.Data;

import java.util.Date;

@Data
public class DqcTaskVo extends  DqcTableRuleVo {

    /**
     * 实例时间
     */
    private Date taskTime;

    /**
     * 负责人
     */
    private String ownerName;

    /**
     * 异常数
     */
    private Integer exceptionNum;

    /**
     * 执行时间
     */
    private Date execTime;

    /**
     * 实例id
     */
    private Long targetTaskId;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态枚举
     */
    private DqcTaskStatusEnum statusType;

    /**
     * 分区
     */
    private String partition;
}
