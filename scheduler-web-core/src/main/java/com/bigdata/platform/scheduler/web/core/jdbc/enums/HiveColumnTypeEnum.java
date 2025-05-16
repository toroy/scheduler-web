/**
 * File generated at: 2018年12月10日下午6:06:52
 */
package com.bigdata.platform.scheduler.web.core.jdbc.enums;

import java.sql.Date;
import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;

import com.bigdata.platform.scheduler.common.constant.DateFormatPattern;
import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.common.util.DateUtil;
import com.bigdata.platform.scheduler.web.core.jdbc.constant.ColumnTypeConstant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * hive字段通用映射字段 
 * 
 * @author 陈泰（周利江）
 * @Date 2018年12月10日下午6:06:52
 *
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum HiveColumnTypeEnum implements ColumnTypeConstant  {

	TINYINT(ColumnTypeConstant.NUM, "数字"), 
	SMALLINT(ColumnTypeConstant.NUM, "数字"), 
	INT(ColumnTypeConstant.NUM, "数字"), 
	BIGINT(ColumnTypeConstant.NUM, "数字"), 
	FLOAT(ColumnTypeConstant.NUM, "数字"), 
	DOUBLE(ColumnTypeConstant.NUM, "数字"), 
	DECIMAL(ColumnTypeConstant.NUM, "数字"), 
	NUMERIC(ColumnTypeConstant.NUM, "数字"), 
	
	CHAR(ColumnTypeConstant.STRING,"字符"), 
	VARCHAR(ColumnTypeConstant.STRING, "字符"), 
	STRING(ColumnTypeConstant.STRING,"字符"), 
	
	BOOLEAN(ColumnTypeConstant.BOOLEAN,"布尔值"),
	
	DATE(ColumnTypeConstant.DATE,"日期"),  
	TIMESTAMP(ColumnTypeConstant.DATE,"日期"), 
	INTERVAL(ColumnTypeConstant.DATE,"日期");

	private String commonType;

	private String desc;
	
	public static Object getDateFormat(Object data) {
		if (data != null) {
			if (data instanceof Date) {
				data = (Date) data;
				return data.toString();
			} else if (data instanceof Timestamp) {
				data = (Timestamp) data;
				Long dataLong = ((Timestamp) data).getTime();
				data = DateUtil.formatUnixStamp(dataLong, DateFormatPattern.YYYY_MM_DD_HH_MM_SS);
				return data.toString();
			}
			return data;
		}
		return null;
	}
	
	// 先完全匹配，没有再向前匹配，防止 INT， INTERVAL 混淆
	public static String getCommonType(String type) {
		Assert.notBlank(type);

		type = type.toUpperCase();
		for (HiveColumnTypeEnum columnType : HiveColumnTypeEnum.values()) {
			if (StringUtils.equalsIgnoreCase(type, columnType.name())) {
				return columnType.commonType;
			}
		}

		for (HiveColumnTypeEnum columnType : HiveColumnTypeEnum.values()) {
			if (StringUtils.startsWith(type, columnType.name())) {
				return columnType.commonType;
			}
		}
		log.error("parse error: type:{}", type);
		return null;
	}
}
