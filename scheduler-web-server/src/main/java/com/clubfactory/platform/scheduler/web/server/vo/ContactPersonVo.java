package com.clubfactory.platform.scheduler.web.server.vo;

import com.clubfactory.platform.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.po.UserInfo;
import com.clubfactory.platform.scheduler.web.core.utils.MaskUtil;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class ContactPersonVo {

    private Long id;

    private String name;

    private String phoneNo;

    private String email;

    private String imRobot;

    private String groups;

    public static ContactPersonVo po2Vo(UserInfo userInfo) {
        ContactPersonVo contactPersonVo = new ContactPersonVo();
        BeanUtil.copyBeanNotNull2Bean(userInfo, contactPersonVo);
        contactPersonVo.setPhoneNo(MaskUtil.maskPhoneNumber(userInfo.getPhoneNo()));
        return contactPersonVo;
    }
}
