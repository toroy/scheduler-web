/**
 * File generated at: 2018年10月16日下午9:54:11
 */
package com.zhugeio.platform.scheduler.web.core.jdbc.enums;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Year;

import com.zhugeio.platform.scheduler.web.core.jdbc.constant.ColumnTypeConstant;
import org.apache.commons.lang3.StringUtils;

import com.zhugeio.platform.scheduler.common.constant.DateFormatPattern;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.common.util.DateUtil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * mysql字段通用类型映射字段
 * 
 * @author 陈泰（周利江）
 * @Date 2018年10月16日下午9:54:11
 *
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum MysqlColumnTypeEnum implements ColumnTypeConstant {

	TINYINT(ColumnTypeConstant.NUM, "数字"), 
	SMALLINT(ColumnTypeConstant.NUM, "数字"), 
	MEDIUMINT(ColumnTypeConstant.NUM,"数字"), 
	INT(ColumnTypeConstant.NUM, "数字"), 
	BIGINT(ColumnTypeConstant.NUM, "数字"), 
	FLOAT(ColumnTypeConstant.NUM, "数字"), 
	DOUBLE(ColumnTypeConstant.NUM, "数字"), 
	DECIMAL(ColumnTypeConstant.NUM, "数字"),
	CHAR(ColumnTypeConstant.STRING,"字符"), 
	VARCHAR(ColumnTypeConstant.STRING, "字符"), 
	TINYBLOB(ColumnTypeConstant.STRING,"字符"), 
	TINYTEXT(ColumnTypeConstant.STRING, "字符"), 
	BLOB(ColumnTypeConstant.STRING,"字符"), 
	TEXT(ColumnTypeConstant.STRING, "字符"), 
	MEDIUMBLOB(ColumnTypeConstant.STRING,"字符"), 
	MEDIUMTEXT(ColumnTypeConstant.STRING, "字符"), 
	LONGBLOB(ColumnTypeConstant.STRING,"字符"), 
	LONGTEXT(ColumnTypeConstant.STRING, "字符"), 
	DATE(ColumnTypeConstant.DATE,"日期"), 
	TIME(ColumnTypeConstant.DATE, "日期"), 
	YEAR(ColumnTypeConstant.DATE,"日期"), 
	DATETIME(ColumnTypeConstant.DATE,"日期"), 
	TIMESTAMP(ColumnTypeConstant.DATE,"日期");

	private String commonType;

	private String desc;

	public static Object getDateFormat(Object data) {
		if (data != null) {
			if (data instanceof Time) {
				data = (Time) data;
				return data.toString();
			} else if (data instanceof Timestamp) {
				data = (Timestamp) data;
				Long dataLong = ((Timestamp) data).getTime();
				data = DateUtil.formatUnixStamp(dataLong, DateFormatPattern.YYYY_MM_DD_HH_MM_SS);
				return data.toString();
			} else if (data instanceof Date) {
				data = (Date) data;
				return data.toString();
			} else if (data instanceof Year) {
				data = (Year) data;
				return data.toString();
			}
			return data;
		}
		return null;
	}

	public static String getCommonType(String type) {
		Assert.notBlank(type);

		type = type.toUpperCase();
		for (MysqlColumnTypeEnum columnType : MysqlColumnTypeEnum.values()) {
			if (StringUtils.equalsIgnoreCase(columnType.name(), type)) {
				return columnType.commonType;
			}
		}
		
		for (MysqlColumnTypeEnum columnType : MysqlColumnTypeEnum.values()) {
			if (StringUtils.startsWith(type, columnType.name())) {
				return columnType.commonType;
			}
		}
		log.error("parse error: type:{}", type);
		return null;
	}

}
