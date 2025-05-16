package com.bigdata.platform.scheduler.web.core.dto;

import java.io.Serializable;
import java.util.List;

import com.bigdata.platform.scheduler.dal.enums.IncrementTypeEnum;

import lombok.Data;

@Data
public class JobColumnDto implements Serializable  {

	private static final long serialVersionUID = 4769342819032795719L;

	/**
	 * 源字段信息
	 */
	private List<String> sourceColumns;
	
	/**
	 * 目标字段信息
	 */
	private List<String> targetColumns;
	
	/**
	 * sql
	 */
	private String sql;
	
	/**
	 * 是否sql
	 */
	private Boolean isSql;
	
	/**
	 * 目标字段
	 */
	private String sqlColumn;
	
	/**
	 * 分片字段
	 */
	private String splitPk;
	
	/**
	 * 增量类型
	 */
	private IncrementTypeEnum incrementType;
	
	/**
	 * 增量字段
	 */
	private String incrementColumn;
	
	/**
	 * where条件
	 */
	private String where;
	
	/**
	 * 是否全选字段
	 */
	private Boolean isAllColumn = false;
}
