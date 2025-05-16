/**
 * File generated at: 2018年11月21日下午4:39:27
 */
package com.bigdata.platform.scheduler.web.core.jdbc.client;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bigdata.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.bigdata.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.bigdata.platform.scheduler.web.core.jdbc.dto.TableDto;
import com.bigdata.platform.scheduler.web.core.jdbc.enums.PostgresqlColumnTypeEnum;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.bigdata.platform.scheduler.common.util.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * pg操作类 
 * 
 * @author 陈泰（周利江）
 * @Date 2018年11月21日下午4:39:27
 *
 */
@Service
public class PostgresqlOperations extends BaseOperations implements Operations {

	private String SHOW_TABLE_SQL = "SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';";
	private String SHOW_TABLE_DESC_SQL = "select relname,description from pg_description\n" + 
			"join pg_class on pg_description.objoid = pg_class.oid\n" + 
			"where  objsubid = 0;";
	private String SHOW_COLUMNS_SQL = "select col_description(a.attrelid,a.attnum) as comment, a.attname AS field,t.typname AS type from pg_class c,pg_attribute a,pg_type t where c.relname = '%s' and a.attnum > 0 and a.attrelid = c.oid and a.atttypid = t.oid ORDER BY a.attnum;";
	
	private final String COLUMN_FIELD = "field";
	private final String COLUMN_TYPE = "type";
	private final String COLUMN_COMMENT = "comment";
	private final String TABLENAME = "relname";
	private final String TABLENAME_SOURCE = "tablename";
	private final String DESCRIPITON = "description";
	
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
		List<Map<String, Object>> tableDatas = jdbcTemplate.queryForList(SHOW_TABLE_SQL, Maps.newHashMap());
		if (CollectionUtils.isEmpty(tableDatas)) {
			return Lists.newArrayList();
		}
		List<Map<String, Object>> mapDatas = jdbcTemplate.queryForList(SHOW_TABLE_DESC_SQL, Maps.newHashMap());
		Map<String, String> newMapData = Maps.newHashMap();
		mapDatas.stream().forEach(mapData -> {
			newMapData.put(mapData.get(TABLENAME).toString(), Optional.ofNullable(mapData.get(DESCRIPITON)).orElse("").toString());
		});
		
		return tableDatas.stream().map(tableData -> {
			TableDto tableDto = new TableDto();
			tableDto.setName(tableData.get(TABLENAME_SOURCE).toString());
			tableDto.setDesc(newMapData.get(tableData.get(TABLENAME_SOURCE).toString()));
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
				Object value = PostgresqlColumnTypeEnum.getDateFormat(dataMap.get(key));
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
