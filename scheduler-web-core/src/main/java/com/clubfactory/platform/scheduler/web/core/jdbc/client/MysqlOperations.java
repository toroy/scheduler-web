/**
 * File generated at: 2018年11月21日下午4:38:59
 */
package com.clubfactory.platform.scheduler.web.core.jdbc.client;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.TableDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.enums.MysqlColumnTypeEnum;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * mysql操作类 
 * 
 * @author 陈泰（周利江）
 * @Date 2018年11月21日下午4:38:59
 *
 */
@Service
public class MysqlOperations extends BaseOperations implements Operations {
	
	private String SHOW_TABLE_SQL = "SELECT TABLE_NAME,TABLE_COMMENT FROM information_schema.TABLES WHERE table_schema='%s'";
	private String SHOW_COLUMNS_SQL = "show full columns from `%s`;";
	private final String COLUMN_FIELD = "field";
	private final String COLUMN_TYPE = "type";
	private final String COLUMN_COMMENT = "comment";
	private final String TABLE_NAME = "TABLE_NAME";
	private final String TABLE_COMMENT = "TABLE_COMMENT";
	
	@Override
	public void init(DbDto dbDto, String dbUrl) {
		super.init(dbDto, dbUrl);
	}
	
	@Override
	public List<TableDto> listTables(String dbUrl) {
		Assert.notNull(dbUrl);
		
		NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbUrl);
		if (jdbcTemplate == null) {
			return Lists.newArrayList();
		}
		
		DbDto dbDto = connectService.getDb(dbUrl);
		List<Map<String, Object>> mapDatas = jdbcTemplate.queryForList(String.format(SHOW_TABLE_SQL, dbDto.getDbName()), Maps.newHashMap());
		if (CollectionUtils.isEmpty(mapDatas)) {
			return Lists.newArrayList();
		}
		
		return mapDatas.stream().map(mapData -> {
			TableDto tableDto = new TableDto();
			tableDto.setName(mapData.get(TABLE_NAME).toString());
			tableDto.setDesc(Optional.ofNullable(mapData.get(TABLE_COMMENT)).orElse("").toString());
			return tableDto;
		}).collect(Collectors.toList());
	}


	@Override
	public List<ColumnDto> listColumns(String dbUrl, String tableName) {
		Assert.notNull(dbUrl);
		Assert.notBlank(tableName, "表名");
		
		NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbUrl);
		if (jdbcTemplate == null) {
			return Lists.newArrayList();
		}
		List<Map<String, Object>> mapDatas = jdbcTemplate.queryForList(String.format(SHOW_COLUMNS_SQL, tableName), Maps.newHashMap());
		if (CollectionUtils.isEmpty(mapDatas)) {
			return Lists.newArrayList();
		}
		return mapDatas.stream()
				.map(mapData -> { 
					ColumnDto columnDto = new ColumnDto();
					columnDto.setName(mapData.get(COLUMN_FIELD).toString());
					columnDto.setType(mapData.get(COLUMN_TYPE).toString());
					columnDto.setDesc(Optional.ofNullable(mapData.get(COLUMN_COMMENT)).orElse("").toString());
			return columnDto;
		}).collect(Collectors.toList());
	}


	@Override
	public List<ColumnDto> listColumns(String dbUrl, String dbName, String tableName) {
		return listColumns(dbUrl, tableName);
	}

	@Override
	public List<Map<String, Object>> queryForList(String dbUrl, String tableName) {
		Assert.notNull(dbUrl);
		Assert.notBlank(tableName, "表名");
		
		List<Map<String, Object>> datas = super.queryForList(dbUrl, tableName);
		datas.stream().forEach(dataMap -> {
			dataMap.keySet().forEach(key -> {
				Object value = MysqlColumnTypeEnum.getDateFormat(dataMap.get(key));
				dataMap.put(key, value);
			});
		});
		
		return datas;
	}
	
	@Override
	public void close(String dbUrl) {
		Assert.notNull(dbUrl);
		super.close(dbUrl);
	}

}
