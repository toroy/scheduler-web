package com.clubfactory.platform.scheduler.web.server.dto;

import java.util.List;

import com.clubfactory.platform.scheduler.common.bean.Pager;
import com.clubfactory.platform.scheduler.dal.enums.JobCategoryEnum;
import com.clubfactory.platform.scheduler.dal.enums.JobCycleTypeEnum;
import com.clubfactory.platform.scheduler.dal.enums.JobStatusEnum;
import com.clubfactory.platform.scheduler.dal.enums.PriorityEnum;

import lombok.Data;

@Data
public class JobQueryDto extends Pager {

	private static final long serialVersionUID = -1186688865159598953L;

	/**
	 * 开始时间
	 */
	private String startTime;
	
	/**
	 * 结束时间
	 */
	private String endTime;
	
	/**
	 * 创建者
	 */
	private String userName;
	
	/**
	 * 团队
	 */
	private String departName;
	
	/**
	 * 任务id
	 */
	private Long id;
	
	/**
	 * 任务名称
	 */
	private String name;
	
	/**
	 * HIVE JAVA PYTHON
	 * 任务类型
	 */
	private String type;
	
	/**
	 * 任务状态
	 */
	private JobStatusEnum status;
	
	/**
	 * 周期类型
	 */
	private JobCycleTypeEnum cycleType;
	
	/**
	 * 优先级
	 */
	private PriorityEnum priority;
	
	/**
	 * 大类
	 */
	private JobCategoryEnum category;
	
	/**
	 * 是否线上
	 */
	private Boolean isOnline;
	
	/**
	 * 状态多选项
	 */
	private List<JobStatusEnum> statuses;
	
	/**
	 * 是否用临时集群
	 */
	private Boolean runOnTmpEmr;
	
	/**
	 * 目标表
	 */
	private String targetTable;
	
	/**
	 * 项目id
	 */
	private Long projectId;

}
