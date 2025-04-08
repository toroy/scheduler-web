package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.po.GroupInfo;
import com.clubfactory.platform.scheduler.dal.po.UserGroupRel;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Data
public class GroupInfoEditDto extends GroupInfoDto {

    /**
     * 联系人组名称
     */
    private Long id;


    /**
     * dto to po
     * @return
     */
    public GroupInfo buildEditPo() {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(this.id);
        groupInfo.setIsDeleted(false);
        Map<String, Object> updateParams = BeanUtil.copyBeanCamel2MapUnder(this,null, Lists.newArrayList("userList", "id"));
        updateParams.put("update_user", LocalUser.getLoginUserId());
        groupInfo.setUpdateParam(updateParams);
        return groupInfo;
    }

    public List<UserGroupRel> buildRefList() {
        if (CollectionUtils.isEmpty(this.getUserList())) {
            return Lists.newArrayList();
        }
        Long loginUserId = LocalUser.getLoginUserId();
        return this.getUserList().stream()
                .map(userInfoId -> {
                    UserGroupRel rel = new UserGroupRel();
                    rel.setUserInfoId(userInfoId);
                    rel.setGroupId(this.id);
                    rel.setCreateUser(loginUserId);
                    rel.setUpdateUser(loginUserId);
                    return rel;
                }).collect(Collectors.toList());
    }
}
