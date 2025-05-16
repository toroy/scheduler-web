package com.bigdata.platform.scheduler.web.server.dto;

import java.util.List;

import com.bigdata.platform.scheduler.web.server.vo.SortByVo;
import com.bigdata.platform.scheduler.common.bean.Pager;
import com.bigdata.platform.scheduler.dal.enums.JobCategoryEnum;
import com.bigdata.platform.scheduler.dal.enums.TaskStatusEnum;

import lombok.Data;

@Data
public class TaskQueryDto extends Pager {

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
	 * 作业id
	 */
	private Long id;
	
	/**
	 * 任务id
	 */
	private Long jobId;
	
	/**
	 * 任务名称
	 */
	private String name;
	
	/**
	 * 任务类型
	 */
	private String type;
	
	/**
	 * 任务状态
	 */
	private TaskStatusEnum status;
	
	/**
	 * 大类
	 */
	private JobCategoryEnum category;
	
	/**
	 * 状态多选
	 */
	private List<TaskStatusEnum> statuses;

	private List<SortByVo> sortByList;
	
	/**
	 * 耗时区间，第一个元素是最小值，第二个元素是最大值
	 */
	private List<Integer> durArea;
	
}
