package com.bigdata.platform.scheduler.web.server.dto;

import com.bigdata.platform.scheduler.web.server.login.LocalUser;
import com.bigdata.platform.scheduler.dal.po.GroupInfo;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class GroupUsersDto {

    /**
     * 组ID
     */
    private Long groupId;

    /**
     * 是否在当前组
     */
    private Boolean isInGroup;

    public GroupInfo buildPo(){
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(groupId);
        groupInfo.setCreateUser(LocalUser.getLoginUserId());
        groupInfo.setOrderBy("update_time desc");
        return groupInfo;
    }
}
