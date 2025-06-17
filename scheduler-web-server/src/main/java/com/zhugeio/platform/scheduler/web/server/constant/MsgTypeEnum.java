package com.zhugeio.platform.scheduler.web.server.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MsgTypeEnum {

	TEXT("text"),
	MARKDOWN("markdown");
	
	private String value;
}
