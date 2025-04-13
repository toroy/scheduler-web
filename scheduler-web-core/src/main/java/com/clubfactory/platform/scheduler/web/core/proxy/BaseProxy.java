package com.clubfactory.platform.scheduler.web.core.proxy;

import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.common.util.Assert;

/**
 * 代理基础类
 *
 * @author zhoulijiang
 * @date 2021/3/30 12:23 下午
 */
public class BaseProxy {

    /**
     * 检查返回的结构
     *
     * @param code
     * @param message
     */
    protected void checkResult(Integer code, String message) {
        Assert.notNull(code);
        this.checkResult(null, code, message);
    }


    /**
     * 检查返回的结构
     *
     * @param isSuccess
     * @param code
     * @param message
     */
    protected void checkResult(Boolean isSuccess, Integer code, String message) {
        Assert.notNull(code);

        if (isSuccess != null) {
            if (isSuccess.equals(false)) {
                throw new BizException(code,  message);
            }
        } else {
            if (code != 0) {
                throw new BizException(code, message);
            }
        }
    }


}
