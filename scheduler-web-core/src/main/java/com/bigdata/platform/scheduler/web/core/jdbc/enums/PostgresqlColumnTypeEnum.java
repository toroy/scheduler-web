/**
 * File generated at: 2018年10月16日下午11:13:49
 */
package com.bigdata.platform.scheduler.web.core.jdbc.enums;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGInterval;

import com.bigdata.platform.scheduler.common.constant.DateFormatPattern;
import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.common.util.DateUtil;
import com.bigdata.platform.scheduler.web.core.jdbc.constant.ColumnTypeConstant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 这里写功能介绍
 * 
 * @author 陈泰（周利江）
 * @Date 2018年10月16日下午11:13:49
 *
 */
@Getter
@AllArgsConstructor
@Slf4j
public enum PostgresqlColumnTypeEnum implements ColumnTypeConstant {

	SMALLINT(ColumnTypeConstant.NUM, "数字"), 
	INT(ColumnTypeConstant.NUM, "数字"), 
	BIGINT(ColumnTypeConstant.NUM,"数字"), 
	DECIMAL(ColumnTypeConstant.NUM, "数字"), 
	NUMERIC(ColumnTypeConstant.NUM, "数字"), 
	REAL(ColumnTypeConstant.NUM, "数字"), 
	DOUBLE(ColumnTypeConstant.NUM, "数字"), 
	SERIAL(ColumnTypeConstant.NUM,"数字"), 
	BIGSERIAL(ColumnTypeConstant.NUM, "数字"), 
	INTERVAL(ColumnTypeConstant.STRING,"字符"), 
	VARCHAR(ColumnTypeConstant.STRING, "字符"), 
	CHAR(ColumnTypeConstant.STRING,"字符"), 
	TEXT(ColumnTypeConstant.STRING, "字符"), 
	DATE(ColumnTypeConstant.DATE,"日期"), 
	TIME(ColumnTypeConstant.DATE,"日期"), 
	TIMESTAMP(ColumnTypeConstant.DATE, "日期");

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
			} else if (data instanceof PGInterval) {
				data = (PGInterval) data;
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
		for (PostgresqlColumnTypeEnum columnType : PostgresqlColumnTypeEnum.values()) {
			if (StringUtils.equalsIgnoreCase(type, columnType.name())) {
				return columnType.commonType;
			}
		}

		for (PostgresqlColumnTypeEnum columnType : PostgresqlColumnTypeEnum.values()) {
			if (StringUtils.startsWith(type, columnType.name())) {
				return columnType.commonType;
			}
		}
		log.error("parse error: type:{}", type);
		return null;
	}

}
