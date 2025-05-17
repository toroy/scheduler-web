package com.bigdata.platform.scheduler.web.server.login;


import lombok.Data;

@Data
public class LoginUserDto {

	private static final long serialVersionUID = -9040884828677253340L;

	/**
	 * 是否管理员
	 */
	private Boolean isAdmin;
	
	/**
	 * 本地用户id，为用户表的主键id
	 */
	private Long localUserId;
	
	/**
	 * 部门名称
	 */
	private String departName;

	private String name;
	
	/**
	 * 单点登录的token，方便直接访问内部其他网站
	 */
	private String token;

	private String alias;

	private String userid;

	private String password;
//
//	public Long uidToLong() {
////		try {
////			return Long.parseLong(getUserid());
////		} catch (Exception e) {
////			return null;
////		}
//	}
	
	public Boolean getIsAdmin() {
		return isAdmin;
	}
}
