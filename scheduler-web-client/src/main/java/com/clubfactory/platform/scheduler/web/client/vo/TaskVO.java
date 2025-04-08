package com.clubfactory.platform.scheduler.web.client.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class TaskVO implements Serializable {

	private static final long serialVersionUID = 5751748875936849872L;
	
	/**
	 * 实例id
	 */
	private Long id;
	
	/**
	 * 任务启动执行时间
	 */
	private Date execTime;

	/**
	 * task时间
	 */
	private Date taskTime;
	
	/**
	 * 开始时间
	 */
	private Date startTime;
	
	/**
	 * 开始时间
	 */
	private Date endTime;
	
	/**
	 * 任务id
	 */
	private Long jobId;
	

	/**
	 * 作业名称
	 */
	private String name;

}
