package com.bigdata.platform.scheduler.web.server.vo;

import com.bigdata.platform.scheduler.dal.po.GroupInfo;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class BasicGroupInfoVo {

    private String groupName;

    private Long groupId;

    public static BasicGroupInfoVo po2Vo(GroupInfo groupInfo) {
        BasicGroupInfoVo basicGroupInfoVo = new BasicGroupInfoVo();
        basicGroupInfoVo.setGroupId(groupInfo.getId());
        basicGroupInfoVo.setGroupName(groupInfo.getGroupName());
        return basicGroupInfoVo;
    }
}
