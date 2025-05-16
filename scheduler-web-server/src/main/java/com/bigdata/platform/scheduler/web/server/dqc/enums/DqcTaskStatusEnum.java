package com.bigdata.platform.scheduler.web.server.dqc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DqcTaskStatusEnum {

    INIT("未运行", null),
    SUCCESS("正常", null),
    BLOCK("阻塞", true),
    ALARM("报警", false);

    private String desc;

    /**
     * 是否阻塞
     */
    private Boolean isBlock;
}
