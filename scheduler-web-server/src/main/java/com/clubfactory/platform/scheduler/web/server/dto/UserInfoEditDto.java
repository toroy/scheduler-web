package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.po.UserInfo;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static com.clubfactory.platform.scheduler.web.core.Constants.MINI_PHONE_NO_MASK_STR;

/**
 * @author xiejiajun
 */
@Data
public class UserInfoEditDto extends UserInfoDto {
    private Long id;

    public UserInfo buildEditPo() {
        this.validateFields();
        UserInfo userInfo = new UserInfo();
        userInfo.setIsDeleted(false);
        userInfo.setId(this.id);
        List<String> ignoreFields = Lists.newArrayList("id", "isCompleteMode");
        if (StringUtils.isNotBlank(this.getPhoneNo()) && StringUtils.contains(this.getPhoneNo(), MINI_PHONE_NO_MASK_STR)) {
            ignoreFields.add("phoneNo");
        }
        Map<String, Object> updateParams = BeanUtil.copyBeanCamel2MapUnder(this,null, ignoreFields);
        updateParams.put("update_user", LocalUser.getLoginUserId());
        userInfo.setUpdateParam(updateParams);
        return userInfo;
    }
}
