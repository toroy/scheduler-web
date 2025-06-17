package com.zhugeio.platform.scheduler.web.core.dto;

import java.io.Serializable;

import com.zhugeio.platform.scheduler.dal.enums.FormatEnum;
import com.zhugeio.platform.scheduler.dal.enums.IncrementTypeEnum;

import lombok.Data;

@Data
public class JobExtCommonDto implements Serializable {

	private static final long serialVersionUID = -4543241024217087584L;

	/**
	 * 任务id
	 */
	private Long jobId;
	
	/**
	 * 目标表名
	 */
	private String targetTable;
	
	/**
	 * 目标数据源id
	 */
	private Long dbTargetId;
	
	/**
	 * 源表
	 */
	private String sourceTable;
	
	/**
	 * 源id
	 */
	private Long dbSourceId;
	
	/**
	 * 存储格式
	 */
	private FormatEnum storageFormat;
	
	/**
	 * 增量字段
	 */
	private String incrementColumn;
	
	/**
	 * 增量类型
	 */
	private IncrementTypeEnum incrementType;
	
	/**
	 * 配置源字段
	 */
	private String targetColumns;
	
	/**
	 * 配置源字段
	 */
	private String sourceColumns;
	
	/**
	 * where条件
	 */
	private String whereSql;
	
	/**
	 * 分片字段
	 */
	private String splitPk;
	
	/**
	 * 并发数
	 */
	private Integer runCount;
}
