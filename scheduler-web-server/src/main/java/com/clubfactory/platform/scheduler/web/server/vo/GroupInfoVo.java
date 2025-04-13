package com.clubfactory.platform.scheduler.web.server.vo;

import com.beust.jcommander.internal.Lists;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.po.GroupInfo;
import com.clubfactory.platform.scheduler.dal.po.UserInfo;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Data
public class GroupInfoVo {

    /**
     * 组ID
     */
    private Long id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 是否为默认组
     */
    private boolean isDefault;

    /**
     * 联系人列表
     */
    private List<GroupUserVo> userList;

    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean aDefault) {
        isDefault = aDefault;
    }


    public static GroupInfoVo po2Vo(GroupInfo groupInfo, List<UserInfo> userInfos, boolean isDefault) {
        Assert.nonNull(groupInfo, "联系人组为空");
        GroupInfoVo groupInfoVo = new GroupInfoVo();
        groupInfoVo.setGroupName(groupInfo.getGroupName());
        groupInfoVo.setId(groupInfo.getId());
        groupInfoVo.setIsDefault(isDefault);
        List<GroupUserVo> groupUserVos = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(userInfos)) {
            groupUserVos = userInfos.stream()
                    .map(userInfo -> GroupUserVo.po2Vo(userInfo, groupInfo))
                    .collect(Collectors.toList());
        }
        groupInfoVo.setUserList(groupUserVos);
        return groupInfoVo;
    }
}
