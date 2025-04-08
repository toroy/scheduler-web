package com.clubfactory.platform.scheduler.web.server.vo;

import com.clubfactory.platform.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.po.GroupInfo;
import com.clubfactory.platform.scheduler.dal.po.UserInfo;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class GroupUserVo {

    /**
     * 联系人ID
     */
    private Long id;

    /**
     * 联系人姓名
     */
    private String name;

    /**
     * 联系人手机号
     */
    private String phoneNo;

    /**
     * 联系人邮箱
     */
    private String email;

    /**
     * 联系人微信机器人地址
     */
    private String imRobot;

    /**
     * 联系组ID
     */
    private Long groupId;

    /**
     * 联系人组名称
     */
    private String groupName;

    public static GroupUserVo po2Vo(UserInfo userInfo, GroupInfo groupInfo) {
        GroupUserVo groupUserVo = new GroupUserVo();
        BeanUtil.copyBeanNotNull2Bean(userInfo, groupUserVo);
        groupUserVo.setGroupId(groupInfo.getId());
        groupUserVo.setGroupName(groupInfo.getGroupName());
        return groupUserVo;
    }
}
