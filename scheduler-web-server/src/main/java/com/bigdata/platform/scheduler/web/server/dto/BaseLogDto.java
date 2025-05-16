package com.bigdata.platform.scheduler.web.server.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class BaseLogDto implements Serializable {


    private static final long serialVersionUID = -5193492208664042117L;


    /**
     * 日志映射关系ID
     */
    private Long logId;

}
