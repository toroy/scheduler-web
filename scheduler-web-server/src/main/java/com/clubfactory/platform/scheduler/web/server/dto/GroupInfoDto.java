package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.po.GroupInfo;
import com.clubfactory.platform.scheduler.dal.po.UserGroupRel;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Data
public class GroupInfoDto {

    /**
     * 联系人组名称
     */
    private String groupName;

    /**
     * 联系人ID列表
     */
    private List<Long> userList;

    /**
     * dto to po
     * @return
     */
    public GroupInfo buildPo() {
        Assert.nonBlank(this.groupName, "联系人组名称不能为空");
        Assert.collectionNonEmpty(userList, "联系人列表不能为空");
        GroupInfo groupInfo = new GroupInfo();
        BeanUtil.copyBeanNotNull2Bean(this, groupInfo);
        LocalUser.completePoInfo(groupInfo);
        return groupInfo;
    }

    public List<UserGroupRel> buildRefList(Long groupId) {
        if (CollectionUtils.isEmpty(this.userList)) {
            return Lists.newArrayList();
        }
        Long loginUserId = LocalUser.getLoginUserId();
        return this.userList.stream()
                .map(userInfoId -> {
                    UserGroupRel rel = new UserGroupRel();
                    rel.setUserInfoId(userInfoId);
                    rel.setGroupId(groupId);
                    rel.setCreateUser(loginUserId);
                    rel.setUpdateUser(loginUserId);
                    return rel;
                }).collect(Collectors.toList());
    }
}
