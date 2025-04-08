package com.clubfactory.platform.scheduler.web.core.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class AssistantJobDto implements IGroupDto, Serializable {

	private static final long serialVersionUID = 4032692329487940583L;
	
	/**
	 * 组名
	 */
	private String groupName;
	
	/**
	 * 用户id
	 */
	private Long userId;
	
	/**
	 * 项目id
	 */
	private Long projectId;
	
	/**
	 * 任务
	 */
	private List<JobCalDto> jobs;

}
