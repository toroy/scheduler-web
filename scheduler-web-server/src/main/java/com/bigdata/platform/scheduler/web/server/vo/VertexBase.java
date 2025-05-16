package com.bigdata.platform.scheduler.web.server.vo;

import java.io.Serializable;
import java.util.Date;

import com.bigdata.platform.scheduler.dal.enums.TaskStatusEnum;

import lombok.Data;

@Data
public class VertexBase implements Serializable {

	private static final long serialVersionUID = 5295981995632102384L;

	/**
	 * 是否自依赖
	 */
	private Boolean isSelfDependent;
	
	/**
	 * 名字
	 */
	private String name;
	
	/**
	 * 主键id
	 */
	private Long id;
	
	/**
	 * 状态
	 */
	private TaskStatusEnum status;
	
	/**
	 * 目标表
	 */
	private String targetTable;
	
	/**
	 * 调度时间
	 */
	private String scheduler;
	
	/**
	 * 开始时间
	 */
	private Date startTime;
	
	/**
	 * 结束时间
	 */
	private Date endTime;
	
	/**
	 * 耗时
	 */
	private Double dur;
	
	/**
	 * 实际执行时间
	 */
	private Date execTime;
	
	/**
	 * 实例时间
	 */
	private Date taskTime;
}
