package com.bigdata.platform.scheduler.web.server.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class AddTaskDto implements Serializable {

	private static final long serialVersionUID = -2567545188128633065L;

	/**
	 * 任务id
	 */
	private Long jobId;
	
	/**
	 * 实例时间
	 */
	private String taskTime;
	
	/**
	 * 实例开始时间
	 */
	private String startTaskTime;
	
	/**
	 * 实例结束时间
	 */
	private String endTaskTime;
}
