package com.bigdata.platform.scheduler.web.server.utils;

import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.common.util.Assert;

/**
 * @author xiejiajun
 */
public class LoginUserUtils {


    /**
     * 获取部门ID
     * @param userDto
     * @return
     */
    public static Integer getDepartmentId(LoginUserDto userDto){
        Assert.notNull(userDto);
//        if (userDto.getDepartment() != null){
//            return userDto.getDepartment().get(0);
//        }
        return null;
    }

}
