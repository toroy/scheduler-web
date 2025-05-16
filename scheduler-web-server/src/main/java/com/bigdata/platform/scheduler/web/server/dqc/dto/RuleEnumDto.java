package com.bigdata.platform.scheduler.web.server.dqc.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RuleEnumDto implements Serializable {

    private static final long serialVersionUID = -5732494605480787635L;

    /**
     * 规则字段
     */
    private List<ContentDto> fields;

    /**
     * 采样方式
     */
    private List<ContentDto> samples;

    /**
     * 校验类型
     */
    private List<ContentDto>  checkTypes;

    /**
     * 校验方式
     */
    private CheckTypeEnumDto checkModes;

    /**
     * 链接方式
     */
    private List<ContentDto> connectorTypes;

    /**
     * 比较方式
     */
    private CheckTypeEnumDto compares;

    @Data
    public static class CheckTypeEnumDto implements  Serializable {

        private static final long serialVersionUID = 3917154039513193985L;

        private List<ContentDto> NUMBER;

        private List<ContentDto> ROLLING;
    }
}
