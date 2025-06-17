package com.zhugeio.platform.scheduler.web.server.constant;

import com.zhugeio.platform.scheduler.dal.enums.IEnum;

import java.beans.ConstructorProperties;

public enum UpdateJobEnums implements IEnum {

    DS_URL("dsUrl"),
    RUN_COUNT("runCount"),
    MQ_COLUMNS("mqColumns");


    private String desc;

    @ConstructorProperties({"desc"})
    private UpdateJobEnums(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }
}
