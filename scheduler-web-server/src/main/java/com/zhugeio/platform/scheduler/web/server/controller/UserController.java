package com.zhugeio.platform.scheduler.web.server.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.UserBizService;
import com.zhugeio.platform.scheduler.web.server.vo.UserInfoVo;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.web.core.vo.UserVO;
import com.zhugeio.platform.scheduler.web.server.login.LocalUser;

import io.swagger.annotations.Api;

@Api(tags = "用户中心")
@RestController
@RequestMapping("/user")
public class UserController {
	
	@Resource
	UserBizService userBizService;

	@GetMapping("get")
	public BaseResult<UserInfoVo> get() {
		LoginUserDto userDto = LocalUser.get();
		UserInfoVo userInfoVo = userBizService.getUserInfo(userDto);
		return new BaseResult<UserInfoVo>(userInfoVo);
	}


	@GetMapping("listByName")
	public BaseResult<Map<String, List<UserVO>>> listByName(String name) {
		List<UserVO> users = userBizService.listByName(name);
		Map<String, List<UserVO>> map = Maps.newHashMap();
		map.put("users", users);
		return new BaseResult<Map<String, List<UserVO>>>(map);
	}


	
	
}