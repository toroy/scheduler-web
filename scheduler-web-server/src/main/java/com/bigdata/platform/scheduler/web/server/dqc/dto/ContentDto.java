package com.bigdata.platform.scheduler.web.server.dqc.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContentDto implements Serializable {

    private static final long serialVersionUID = 6768668239785799504L;

    public ContentDto() {
    }

    public ContentDto(String desc, Object value) {
        this.desc = desc;
        this.value = value;
    }

    /**
     * 描述
     */
    private String desc;

    /**
     * 值
     */
    private Object value;
}
