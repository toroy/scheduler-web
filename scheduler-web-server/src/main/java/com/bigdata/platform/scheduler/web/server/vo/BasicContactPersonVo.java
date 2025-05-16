package com.bigdata.platform.scheduler.web.server.vo;

import com.bigdata.platform.scheduler.common.util.BeanUtil;
import com.bigdata.platform.scheduler.dal.po.UserInfo;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class BasicContactPersonVo {

    private Long id;

    private String name;

    public static BasicContactPersonVo po2Vo(UserInfo userInfo) {
        BasicContactPersonVo contactPersonVo = new BasicContactPersonVo();
        BeanUtil.copyBeanNotNull2Bean(userInfo, contactPersonVo);
        return contactPersonVo;
    }
}
