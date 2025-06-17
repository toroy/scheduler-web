package com.zhugeio.platform.scheduler.web.server.vo;

import java.io.Serializable;
import java.util.List;

import com.zhugeio.platform.scheduler.web.core.vo.DutyVO;

import lombok.Data;

@Data
public class UserInfoVo implements Serializable {

	private static final long serialVersionUID = -3989181488043492977L;

	/**
	 * 用户名字
	 */
	private String name;
	
	/**
	 * 头像
	 */
	private String avatar;
	
	/*
	 * 是否管理员
	 */
	private Boolean isAdmin;
	
	/**
	 * 邮箱地址
	 */
	private String email;

	/**
	 * 手机号
	 */
	private String phoneNo;

	/**
	 * 微信机器人URL
	 */
	private String imRobot;
	
	/**
	 * 值班人员
	 */
	private List<DutyVO> dutys;
}
