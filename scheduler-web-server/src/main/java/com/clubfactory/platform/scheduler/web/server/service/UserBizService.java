package com.clubfactory.platform.scheduler.web.server.service;

import com.clubfactory.platform.scheduler.dal.po.UserInfo;
import com.clubfactory.platform.scheduler.web.core.service.DutyService;
import com.clubfactory.platform.scheduler.web.core.service.UserInfoService;
import com.clubfactory.platform.scheduler.web.core.service.UserService;
import com.clubfactory.platform.scheduler.web.core.vo.DepartmentVo;
import com.clubfactory.platform.scheduler.web.core.vo.DutyVO;
import com.clubfactory.platform.scheduler.web.core.vo.UserVO;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.vo.UserInfoVo;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserBizService {
	
	@Resource
	UserService userService;
	@Resource
	DutyService dutyService;
	@Resource
	UserInfoService userInfoService;

	public UserInfoVo getUserInfo(LoginUserDto userDto) {
		String userName = userService.getUserName(userDto.getLocalUserId());
		List<DutyVO> dutyVOs = dutyService.onDuty();
		UserInfoVo userInfoVo = new UserInfoVo();
		userInfoVo.setDutys(dutyVOs);
		userInfoVo.setName(userName);
		userInfoVo.setIsAdmin(userDto.getIsAdmin());
		userInfoVo.setAvatar(userDto.getAvatar());
		userInfoVo.setEmail(userDto.getEmail());

		UserInfo userInfoWhere = new UserInfo();
		userInfoWhere.setIsDeleted(false);
		userInfoWhere.setUserId(userDto.getLocalUserId());
		UserInfo userInfo = this.userInfoService.get(userInfoWhere);
		if (userInfo != null) {
			userInfoVo.setPhoneNo(userInfo.getPhoneNo());
			userInfoVo.setImRobot(userInfo.getImRobot());
			if (StringUtils.isNotBlank(userInfo.getEmail())) {
				userInfoVo.setEmail(userInfo.getEmail());
			}
		}
		return userInfoVo;
	}
	
	public List<UserVO> listByName(String name) {
		if (StringUtils.isBlank(name)) {
			return Lists.newArrayList();
		}
		List<UserVO>  userVos = userService.listByName(name);
		return userVos;
	}


	/**
	 * 列出所有部门信息
	 * @return
	 */
	@Deprecated
	public List<DepartmentVo> listDepartments(){
		return userService.listDepartments();
	}
}
