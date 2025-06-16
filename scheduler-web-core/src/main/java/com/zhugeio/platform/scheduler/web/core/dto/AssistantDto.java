package com.zhugeio.platform.scheduler.web.core.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class AssistantDto implements Serializable {

	private static final long serialVersionUID = -7216879359064670295L;

	/**
	 * 项目id
	 */
	private Long projectId;
	
	/**
	 * 脚本id
	 */
	private Long scriptId;
	
}
