package com.zhugeio.platform.scheduler.web.server.dto;


import lombok.Setter;

import java.io.Serializable;
/**
 * @author xiejiajun
 */
public class LoggerDto extends BaseLogDto implements Serializable {
    private static final long serialVersionUID = -163374017217521469L;


    /**
     * 日志读取起始offset
     */
    @Setter
    private Integer offset;

    /**
     * 本次拉取的最大行数
     */
    @Setter
    private Integer size;

    public Integer getOffset() {
        return offset == null || offset < 0 ? 0 : offset;
    }

    public Integer getSize() {
        return size == null || size <= 0 ? 50 : size;
    }
}
