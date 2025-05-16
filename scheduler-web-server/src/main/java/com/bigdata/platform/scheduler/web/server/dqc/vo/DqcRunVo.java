package com.bigdata.platform.scheduler.web.server.dqc.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DqcRunVo implements Serializable {

    private static final long serialVersionUID = 3317899458388685747L;

    /**
     * 实例id
     */
    private Long taskId;

    /**
     * 规则名称
     */
    private String ruleName;
}
