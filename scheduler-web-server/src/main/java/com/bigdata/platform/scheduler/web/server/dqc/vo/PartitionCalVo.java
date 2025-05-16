package com.bigdata.platform.scheduler.web.server.dqc.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PartitionCalVo implements Serializable  {

    private static final long serialVersionUID = 2447954667933816795L;

    /**
     * 实例时间
     */
    private String taskTime;

    /**
     * 结果数据
     */
    private String result;
}

