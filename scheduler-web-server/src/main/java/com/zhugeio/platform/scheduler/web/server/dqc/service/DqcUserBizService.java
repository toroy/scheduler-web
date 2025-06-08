package com.zhugeio.platform.scheduler.web.server.dqc.service;

import com.zhugeio.platform.scheduler.web.server.dqc.dto.DqcUserDto;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.common.util.Assert;
//import com.clubfactory.platform.meta.client.service.MetaClientService;
import com.zhugeio.platform.scheduler.web.core.service.UserService;
import com.zhugeio.platform.scheduler.web.core.vo.UserVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

@Service
public class DqcUserBizService {

    @Resource
    UserService userService;
//    @Resource
//    MetaClientService metaClientService;

    public List<UserVO> list(String name) {
        List<UserVO> userVOS = userService.listByName(name);
        Iterator<UserVO> iterator = userVOS.iterator();
        while (iterator.hasNext()) {
            UserVO userVO = iterator.next();
            if (userVO.getId() == -1L) {
                iterator.remove();
            }
        }
        return userVOS;
    }

    public Boolean edit(DqcUserDto dto, LoginUserDto userDto) {
        Assert.notNull(dto);
        Assert.notBlank(dto.getDbName());
        Assert.notBlank(dto.getTableName());
        Assert.notNull(dto.getId(), "用户");

        UserVO userVO = userService.getUserInfoById(dto.getId());
//        BaseResult<Boolean> baseResult = metaClientService.saveTableOwner(dto.getDbName(), dto.getTableName(), userVO.getUid(), userDto.getUserid());
//        if (baseResult.isSuccess().equals(false)) {
//            throw new BizException(ErrorCode.API_META_ERROR.getErrorCode(), baseResult.getMessage());
//        }
        return true;
    }

}
