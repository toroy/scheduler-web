package com.zhugeio.platform.scheduler.web.client.dto;

import java.io.Serializable;

import com.zhugeio.platform.scheduler.web.client.enums.DbTypeEnum;

import lombok.Data;

@Data
public class TableConnectDto implements Serializable {

	/**
	 * 表名
	 */
	private String tableName;
	
	/**
	 * 数据库链接host地址
	 */
	private String dbHost;
	
	/**
	 * 数据库名称
	 */
	private String dbName;
	
	/**
	 * 数据库类型
	 */
	private DbTypeEnum dbType;
}
