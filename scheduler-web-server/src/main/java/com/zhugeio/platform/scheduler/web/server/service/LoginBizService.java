package com.zhugeio.platform.scheduler.web.server.service;

import com.alibaba.druid.util.Utils;
import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.common.util.BeanUtil;
import com.zhugeio.platform.scheduler.dal.po.Team;
import com.zhugeio.platform.scheduler.dal.po.User;
import com.zhugeio.platform.scheduler.web.core.service.TeamService;
import com.zhugeio.platform.scheduler.web.core.service.UserService;
import com.zhugeio.platform.scheduler.web.core.utils.LoginGuavaCacheUtil;
import com.zhugeio.platform.scheduler.web.core.vo.UserVO;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.utils.LoginUserUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
    @Resource
    TeamService teamService;
    public Boolean register(LoginUserDto userDto) {
        Assert.notNull(userDto, "对象");
        Assert.notNull(userDto.getPassword(), "密码");
        Assert.nonNull(userDto.getUserid(), "账号");
        Assert.nonNull(userDto.getDepartName(), "部门");

        UserVO userVO = userService.getUserInfoByUid(userDto.getUserid().trim());
        if (userVO != null) {
            throw new BizException("用户名已注册");
        }

        String departName = userDto.getDepartName().trim();
        Integer departId = departName.hashCode();

        Long userId = saveUser(userDto, departId, departName);

        saveTeam(departId, departName, userId);

        return true;
    }

    private void saveTeam(Integer departId, String departName, Long userId) {
        Team team = new Team();
        team.setDepartId(departId);
        team.setDepartName(departName);
        team.setIsDeleted(false);
        team.setCreateUser(userId);
        team.setUpdateUser(userId);
        Team teamRes = teamService.get(team);
        if (teamRes == null) {
            teamService.save(team);
        }
    }

    private Long saveUser(LoginUserDto userDto, Integer departId, String departName) {
        User user = new User();
        user.setPassword(Utils.md5(userDto.getPassword().trim()));
        user.setUid(userDto.getUserid().trim());
        user.setAlias(userDto.getAlias() != null ? userDto.getAlias().trim() : userDto.getUserid().trim());
        user.setDepartName(departName);
        user.setDepartId(departId);
        user.setName(userDto.getName().trim());
        user.setIsAdmin(false);
        User userRes = userService.save(user);
        return userRes.getId();
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

    public void logout(String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        LoginGuavaCacheUtil.clear(token);
    }
}
