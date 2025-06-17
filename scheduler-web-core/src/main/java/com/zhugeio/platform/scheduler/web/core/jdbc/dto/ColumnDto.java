/**
 * File generated at: 2018年10月16日下午12:00:10
 */
package com.zhugeio.platform.scheduler.web.core.jdbc.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 字段对象 
 * 
 * @author 陈泰（周利江）
 * @Date 2018年10月16日下午12:00:10
 *
 */
@Data
public class ColumnDto implements Serializable {

	private static final long serialVersionUID = -7240479268573254080L;

	/**
	 * 字段名
	 */
	private String name;
	
	/**
	 * 字段类型名
	 */
	private String type;
	
	/**
	 * 业务类型名
	 */
	private String desc;
	
	/**
	 * 是否是新字段
	 */
	private Boolean isNew;
	
}
