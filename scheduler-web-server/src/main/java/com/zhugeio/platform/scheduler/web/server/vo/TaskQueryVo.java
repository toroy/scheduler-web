package com.zhugeio.platform.scheduler.web.server.vo;

import java.io.Serializable;
import java.util.Date;

import com.zhugeio.platform.scheduler.dal.enums.JobCategoryEnum;

import lombok.Data;

@Data
public class TaskQueryVo implements Serializable {

	private static final long serialVersionUID = -4539396000322873969L;

	/**
	 * 任务id
	 */
	private Long id;
	
	/**
	 * 任务名称
	 */
	private String name;
	
	/**
	 * 类型
	 */
	private String typeDesc;
	
	/**
	 * 团队名称
	 */
	private String departName;
	
	/**
	 * 创建者
	 */
	private String userName;
	
	/**
	 * 开始时间
	 */
	private String startTimeStr;
	
	/**
	 * 实例时间
	 */
	private String taskTimeStr;
	
	/**
	 * 状态
	 */
	private String statusDesc;
	
	/**
	 * 任务id
	 */
	private Long jobId;
	
	/**
	 * 重试次数 
	 */
	private Integer retryCount;
	
	/**
	 * 重试最大次数
	 */
	private Integer retryMax;
	
	/**
	 * 运行时长
	 */
	private Long dur;
	
	/**
	 * 调度机名称
	 */
	private String machineName;

	/**
	 * 脚本名称
	 */
	private String scriptName;

	/**
	 * 脚本名称
	 */
	private String scriptId;
	
	/**
	 * 脚本名称
	 */
	private Integer scriptVersion;
	
	/**
	 * 实例执行时间
	 */
	private String execTimeStr;
	
	/**
	 * 结束时间
	 */
	private String endTimeStr;
	
	/**
	 * 任务类型
	 */
	private String jobType;
	
	/**
	 * 是否生产实例
	 */
	private String isProduce;
	
	/**
	 * 是否生产实例（布尔值，给前端区别用）
	 */
	private Boolean isOnline;
	
	/**
	 * 修改日期
	 */
	private Date updateTime;
	
	/**
	 * 任务分类
	 */
	private JobCategoryEnum category;
}
