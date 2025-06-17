package com.zhugeio.platform.scheduler.web.server.vo;

import com.zhugeio.platform.scheduler.common.util.BeanUtil;
import com.zhugeio.platform.scheduler.dal.po.UserInfo;
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
