package com.bigdata.platform.scheduler.web.server.service;

import com.alibaba.druid.util.Utils;
import com.bigdata.platform.scheduler.common.exception.BizException;
import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.common.util.BeanUtil;
import com.bigdata.platform.scheduler.dal.po.User;
import com.bigdata.platform.scheduler.web.core.service.UserService;
import com.bigdata.platform.scheduler.web.core.utils.GuavaCacheUtil;
import com.bigdata.platform.scheduler.web.core.utils.LoginGuavaCacheUtil;
import com.bigdata.platform.scheduler.web.core.vo.UserVO;
import com.bigdata.platform.scheduler.web.server.login.LocalUser;
import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.web.server.utils.LoginUserUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.UUID;

import static com.alibaba.druid.util.Utils.md5;

/**
 * TODO
 *
 * @author zhoulijiang
 * @date 2025/5/17 23:10
 **/
@Service
public class LoginBizService {

    @Resource
    UserService  userService;
    public Boolean register(LoginUserDto userDto) {
        Assert.notNull(userDto, "对象");
        Assert.notNull(userDto.getPassword(), "密码");
        Assert.nonNull(userDto.getUserid(), "账号");

        UserVO userVO = userService.getUserInfoByUid(userDto.getUserid().trim());
        if (userVO != null) {
            throw new BizException("用户名已注册");
        }

        User user = new User();
        user.setPassword(Utils.md5(userDto.getPassword().trim()));
        user.setUid(userDto.getUserid().trim());
        user.setAlias(userDto.getAlias() != null ? userDto.getAlias().trim() : userDto.getUserid().trim());
        user.setDepartName(userDto.getDepartName().trim());
        user.setName(userDto.getName().trim());
        user.setIsAdmin(false);
        userService.save(user);
        return true;
    }

    public LoginUserDto login(LoginUserDto userDto) {
        Assert.notNull(userDto, "对象");
        Assert.notNull(userDto.getPassword(), "密码");
        Assert.nonNull(userDto.getUserid(), "账号");

        UserVO userVO = userService.getUserInfoByUid(userDto.getUserid());
        if (userVO == null) {
            throw new BizException("账号不存在");
        }
        if (!StringUtils.equals(userVO.getPassword(), md5(userDto.getPassword().trim()))) {
            throw new BizException("密码错误");
        }

        BeanUtil.copyBeanNotNull2Bean(userVO, userDto);
        userDto.setLocalUserId(userVO.getId());

        String token = LoginUserUtils.getUniqueString();
        userDto.setToken(token);

        LoginGuavaCacheUtil.put(token, userDto);

        return userDto;
    }
}
