package com.bigdata.platform.scheduler.web.server.dqc.controller;

import com.bigdata.platform.scheduler.web.server.dqc.dto.DqcUserDto;
import com.bigdata.platform.scheduler.web.server.dqc.service.DqcUserBizService;
import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.common.bean.BaseResult;
import com.bigdata.platform.scheduler.dal.po.User;
import com.bigdata.platform.scheduler.web.core.vo.UserVO;
import com.bigdata.platform.scheduler.web.server.login.LocalUser;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Api(tags = "dqc-用户")
@RequestMapping("/dqc/user")
@RestController
public class DqcUserController {

    @Resource
    DqcUserBizService dqcUserBizService;

    @ApiOperation(value="owner列表", notes="owner列表")
    @PostMapping("list")
    public BaseResult<Map<String, List<UserVO>>> list(@RequestBody(required = false) User user) {
        List<UserVO> userVOS = dqcUserBizService.list(Optional.ofNullable(user).orElse(new User()).getName());
        Map<String, List<UserVO>> map = Maps.newHashMap();
        map.put("rows", userVOS);
        return new BaseResult<>(map);
    }

    @ApiOperation(value="修改owner", notes="修改owner")
    @PostMapping("editOwner")
    public BaseResult<Boolean> editOwner(@RequestBody DqcUserDto dqcUserDto) {
        LoginUserDto userDto = LocalUser.get();
        return new BaseResult<>(dqcUserBizService.edit(dqcUserDto, userDto));
    }
}
