package com.zhugeio.platform.scheduler.web.server.dqc.vo;

import com.zhugeio.platform.scheduler.web.server.dqc.dto.DqcTableDto;
import lombok.Data;

@Data
public class DqcDetailVo extends DqcTableDto {

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 关联的任务id
     */
    private Long jobId;

    /**
     * 关联的任务名称
     */
    private String jobName;
}
