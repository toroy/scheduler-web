package com.clubfactory.platform.scheduler.web.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GroupStatusEnum {

	EXIST_UNSELF("已经存在，但不是用户本人创建的"),
	EXIST("已经存在"),
	NOT_EXIST("没有存在");
	
	private String desc;
}
