package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.scheduler.dal.po.GroupInfo;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * @author xiejiajun
 */
@Data
public class GroupInfoListDto {
    /**
     * 用于模糊查询的组名
     */
    private String groupName;

    public GroupInfo buildPo(){
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setIsDeleted(false);
        if (StringUtils.isNotBlank(groupName)) {
            groupInfo.setGroupName(groupName);
        }
        groupInfo.setCreateUser(LocalUser.getLoginUserId());
        groupInfo.setOrderBy("update_time desc");
        return groupInfo;
    }
}
