package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.enums.AlarmNoticeTypeEnum;
import com.clubfactory.platform.scheduler.dal.po.UserInfo;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.clubfactory.platform.scheduler.web.core.Constants.MINI_PHONE_NO_MASK_STR;

/**
 * @author xiejiajun
 */
@Data
public class UserInfoDto {

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phoneNo;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 机器人
     */
    private String imRobot;

    /**
     * 是否为信息补全模式
     */
    private Boolean isCompleteMode;

    /**
     * 用户信息归一化
     */
    public void normalizedInfo() {
        if (StringUtils.isNotBlank(phoneNo)) {
            this.phoneNo = phoneNo.trim();
        }
        if (StringUtils.isNotBlank(email)) {
            this.email = email.trim();
        }
        if (StringUtils.isNotBlank(imRobot)) {
            this.imRobot = imRobot.trim();
        }
    }

    /**
     * dto to po
     * @param currentUserId
     * @return
     */
    public UserInfo buildPo(Long currentUserId) {
        this.validateFields();
        UserInfo userInfo = new UserInfo();
        BeanUtil.copyBeanNotNull2Bean(this, userInfo);
        if (BooleanUtils.isTrue(isCompleteMode)) {
            userInfo.setUserId(currentUserId);
        }
        return userInfo;
    }

    /**
     * @param userInfoId
     * @param defaultGroupId
     * @return
     */
    public UserInfo buildEditPo(Long userInfoId, Long defaultGroupId) {
        this.validateFields();
        UserInfo userInfo = new UserInfo();
        userInfo.setIsDeleted(false);
        userInfo.setId(userInfoId);
        Map<String, Object> updateParams = BeanUtil.copyBeanCamel2MapUnder(this, null, Lists.newArrayList("isCompleteMode"));
        updateParams.put("main_group_id", defaultGroupId);
        userInfo.setUpdateParam(updateParams);
        return userInfo;
    }

    /**
     * 字段格式校验
     */
    protected void validateFields() {
        this.normalizedInfo();
        Assert.isTrue(AlarmNoticeTypeEnum.EMAIL.isValid(this.email), "邮箱地址不合法");
        Assert.isTrue(AlarmNoticeTypeEnum.IM.isValid(this.imRobot), "微信机器人地址不合法");
        Assert.isTrue(StringUtils.isBlank(this.phoneNo)  || StringUtils.contains(this.phoneNo, MINI_PHONE_NO_MASK_STR)
                || AlarmNoticeTypeEnum.PHONE_NO.isValid(this.phoneNo), "手机号格式不合法");
    }
}
