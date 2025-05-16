package com.bigdata.platform.scheduler.web.server.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class MqEnumVo implements Serializable {

	private static final long serialVersionUID = -2144756864763793126L;

	/**
	 * 字段类型
	 */
	private List<JobEnumVo.Content> columnTypes;
	
	/**
	 * kafak集群
	 */
	private List<JobEnumVo.Content> dbs;
	
	/**
	 * 偏移类型
	 */
	private List<JobEnumVo.Content> offsetTypes;
	
	/**
	 * 数据格式
	 */
	private List<JobEnumVo.Content> dataTypes;
}
