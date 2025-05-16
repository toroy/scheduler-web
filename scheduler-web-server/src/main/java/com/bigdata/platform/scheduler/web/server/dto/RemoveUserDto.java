package com.bigdata.platform.scheduler.web.server.dto;

import com.bigdata.platform.scheduler.dal.po.UserGroupRel;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class RemoveUserDto {

    /**
     * 联系人组ID
     */
    private Long groupId;

    /**
     * 要移除的联系人ID
     */
    private Long userId;

    public UserGroupRel buildRelPo() {
        UserGroupRel rel = new UserGroupRel();
        rel.setGroupId(this.groupId);
        rel.setUserInfoId(this.userId);
        return rel;
    }
}
