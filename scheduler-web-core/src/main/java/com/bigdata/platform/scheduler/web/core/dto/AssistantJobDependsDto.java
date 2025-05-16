package com.bigdata.platform.scheduler.web.core.dto;

import lombok.Data;

@Data
public class AssistantJobDependsDto extends GraphDto {

	private static final long serialVersionUID = 2971709856249813552L;

	/**
	 * 用户id
	 */
	private Long userId;
	
	/**
	 * 组名称
	 */
	private String groupName;

}
