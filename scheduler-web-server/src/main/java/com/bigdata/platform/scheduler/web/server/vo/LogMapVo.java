package com.bigdata.platform.scheduler.web.server.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
@Builder
public class LogMapVo {

    private Long logId;

    private String logName;

    private String logHost;

    private String logPath;
    
    private Long taskId;
    
    /**
     * 任务名
     */
    private String jobName;
    
    
    /**
     * 任务id
     */
    private Long jobId;
    
}

