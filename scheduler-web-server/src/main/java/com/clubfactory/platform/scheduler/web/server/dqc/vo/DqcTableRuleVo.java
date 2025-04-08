package com.clubfactory.platform.scheduler.web.server.dqc.vo;

import com.clubfactory.platform.scheduler.web.server.dqc.dto.DqcTableDto;
import lombok.Data;

@Data
public class DqcTableRuleVo extends DqcTableDto {

    private static final long serialVersionUID = 8069734835783121469L;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 规则名
     */
    private Integer ruleNum;

    /**
     * 规则Id
     */
    private String ruleId;

    /**
     * 用户id
     */
    private Long userId;
}
