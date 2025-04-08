package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.common.bean.Pager;
import com.clubfactory.platform.scheduler.dal.po.UserInfo;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author xiejiajun
 */
@Data
public class UserInfoPagerDto extends Pager {

    private String name;

    public UserInfo buildQueryPo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setIsDeleted(false);
        userInfo.setPageSize(this.getPageSize());
        userInfo.setPageNo(this.getPageNo());
        if (StringUtils.isNotBlank(name)) {
            userInfo.setName(this.getName());
        }
        userInfo.setCreateUser(LocalUser.getLoginUserId());
        userInfo.setOrderBy("update_time desc");
        return userInfo;
    }
}
