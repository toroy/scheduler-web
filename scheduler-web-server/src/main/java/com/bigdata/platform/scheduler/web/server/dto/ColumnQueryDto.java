package com.bigdata.platform.scheduler.web.server.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ColumnQueryDto implements Serializable {

	private static final long serialVersionUID = 5657837974258510127L;

	/**
	 * 源表id
	 */
	private Long dbSourceId;
	
	/**
	 * 目标表id
	 */
	private Long dbTargetId;
	
	/**
	 * 源表
	 */
	private String sourceTable;
	
	/**
	 * 目标表
	 */
	private String targetTable;
}
