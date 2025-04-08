package com.clubfactory.platform.scheduler.web.core.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.clubfactory.platform.scheduler.dal.po.Team;
import lombok.Data;

@Data
public class TeamVO extends Team {

    @JSONField(name = "parentid")
    private Integer parentId;
}
