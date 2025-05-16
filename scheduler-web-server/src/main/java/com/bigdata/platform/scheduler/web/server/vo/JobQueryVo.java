package com.bigdata.platform.scheduler.web.server.vo;

import java.io.Serializable;

import com.bigdata.platform.scheduler.dal.enums.JobCategoryEnum;
import com.bigdata.platform.scheduler.dal.enums.JobStatusEnum;

import lombok.Data;

 @Data
public class JobQueryVo implements Serializable {

	private static final long serialVersionUID = -7320476928834362701L;

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
	 * 创建人id
	 */
	private Long createUser;
	
	/**
	 * 创建时间
	 */
	private String createTimeStr;
	
	/**
	 * 状态
	 */
	private String statusDesc;
	
	/**
	 * 状态枚举
	 */
	private JobStatusEnum status;
	
	/**
	 * 优先级
	 */
	private String priorityDesc;
	
	/**
	 * 是否允许配依赖
	 */
	private Boolean isAllowDepend;
	
	/**
	 * 是否允许补录
	 */
	private Boolean isAllowRepair;
	
	/**
	 * 是否允许编辑
	 */
	private Boolean isEdit;
	
	/**
	 * 任务周期
	 */
	private String cycleTypeStr;
	
	/**
	 * 调度定时
	 */
	private String schedulerTimeStr;
	
	/**
	 * 重试次数
	 */
	private Integer retryMax;
	
	/**
	 * 调度机名字
	 */
	private String machineName;
	
	/**
	 * 审核人
	 */
	private String checkUserName;
	
	/**
	 * 是否线上
	 */
	private Boolean isOnline;
	
	/**
	 * 脚本id
	 */
	private Long scriptId;

     /**
      * 脚本版本
      */
     private Integer scriptVersion;

	
	/**
	 * id索引
	 */
	private String idIndex;
	
	/**
	 * 大类
	 */
	private JobCategoryEnum category;
	
	/**
	 * 小类
	 */
	private String type;
	
	/**
	 * 目标表
	 */
	private String targetTable;
	
	/**
	 * 项目名
	 */
	private String projectName;
	
	/**
	 * 是否允许删除
	 */
	private Boolean isAllowDel;
	
}
