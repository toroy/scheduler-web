package com.clubfactory.platform.scheduler.web.core.dto;

import java.io.Serializable;
import java.util.List;

import com.clubfactory.platform.scheduler.dal.enums.DependTypeEnum;

import lombok.Data;

@Data
public class AssistantSimpleJobDependsDto implements Serializable {

	private static final long serialVersionUID = -6946304675587871906L;

	/**
	 * 用户id
	 */
	private Long userId;
	
	/**
	 * 组名称
	 */
	private String groupName;
	
	/**
	 * 父节点依赖
	 */
	private List<JobDependsDto> jobDepends;
	
	@Data
	public static class JobDependsDto implements Serializable {
		
		/**
		 * 任务名
		 */
		private String name;
		
		/**
		 * 任务id
		 */
		private Long id;
		
		/**
		 * 枚举类型
		 */
		private DependTypeEnum type;
		
		/**
		 * 父任务名称
		 */
		private String parentName;
		
		/**
		 * 父任务id
		 */
		private Long parentId;
	}
	
}
