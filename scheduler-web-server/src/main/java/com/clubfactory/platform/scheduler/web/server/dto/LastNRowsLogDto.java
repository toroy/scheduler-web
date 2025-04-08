package com.clubfactory.platform.scheduler.web.server.dto;


import lombok.Setter;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
public class LastNRowsLogDto extends BaseLogDto implements Serializable {
    private static final long serialVersionUID = -163374017217521469L;

    /**
     * 本次拉取的最大行数(最后rows行）
     */
    @Setter
    private Integer rows;

    public Integer getRows() {
        return rows == null || rows <= 0 ? 50 : rows;
    }
}
