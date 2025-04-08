package com.clubfactory.platform.scheduler.web.server.vo;

import java.io.Serializable;
import java.util.List;

import com.clubfactory.platform.scheduler.web.server.vo.JobEnumVo.Content;

import lombok.Data;

@Data
public class MqEnumVo implements Serializable {

	private static final long serialVersionUID = -2144756864763793126L;

	/**
	 * 字段类型
	 */
	private List<Content> columnTypes;
	
	/**
	 * kafak集群
	 */
	private List<Content> dbs;
	
	/**
	 * 偏移类型
	 */
	private List<Content> offsetTypes;
	
	/**
	 * 数据格式
	 */
	private List<Content> dataTypes;
}
