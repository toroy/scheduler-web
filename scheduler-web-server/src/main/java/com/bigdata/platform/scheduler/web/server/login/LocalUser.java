package com.bigdata.platform.scheduler.web.server.login;

import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.dal.po.BasePO;

public class LocalUser {

	private static ThreadLocal<LoginUserDto> user = new ThreadLocal<LoginUserDto>();

	public static LoginUserDto get() {
		return user.get();
	}

	public static void set(LoginUserDto userDto) {
		user.set(userDto);
	}

	public static Long getLoginUserId() {
		LoginUserDto loginUserDto = get();
		Assert.nonNull(loginUserDto, "登陆用户信息为空");
		Long userId = loginUserDto.getLocalUserId();
		Assert.nonNull(userId, "登陆用户ID为空");
		return userId;
	}


	public static void completePoInfo(BasePO po) {
		Long currentUserId = getLoginUserId();
		po.setCreateUser(currentUserId);
		po.setUpdateUser(currentUserId);
	}
}
