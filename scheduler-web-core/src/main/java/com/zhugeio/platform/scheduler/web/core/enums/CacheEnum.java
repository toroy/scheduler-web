package com.zhugeio.platform.scheduler.web.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CacheEnum {

	CONNECT("connect_%s", "链接缓存"),
	DRUID_DATA_SOURCE("druid_%s", "Druid缓存"),
	DB_DATA("db_data_%s", "Druid缓存"),
	USER("user_%s", "用户信息缓存"),
	USER_UID("user_uid_%s", "根据uid缓存用户名信息"),
	USER_UID_ID("user_uid_id_%s", "根据uid缓存用户ID信息"),
    USERID("user_id_%s", "用户id信息缓存"),
	TOEKN("token","token缓存"),
	USER_ALIAS("user_alias_%s", "用户别名缓存"),
	DEPENT("depent_%s","部门缓存"),
	SYS_CONFIG("sys_config_%s","系统参数"),
	TOEKN_CHECK("token_check_%s", "token验证缓存"),
	TOKEN_USER("token_user_%s", "token获取用户信息缓存");
	
	private String prefix;
	
	private String desc;
	
	
	public String getKey(Object name) {
		return String.format(this.prefix, name);
	}
	
	public String getKey() {
		return String.format(this.prefix, "all");
	}
}
