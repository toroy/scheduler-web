package com.bigdata.platform.scheduler.web.server.dqc.dto;

import lombok.Data;

@Data
public class DqcJobDto extends DqcTableDto {

    /**
     * 关联任务id
     */
    private Long jobId;
}
