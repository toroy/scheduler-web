package com.clubfactory.platform.scheduler.web.core.jdbc.client;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.scheduler.web.core.jdbc.constant.Constant;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.TableDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.enums.PostgresqlColumnTypeEnum;
import com.clubfactory.platform.scheduler.web.core.jdbc.enums.RedshiftColumnTypeEnum;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

/**
 * 基础操作类
 * @author 周利江
 *
 */
@Service
@Slf4j
public class RedshiftOperations extends BaseOperations implements Operations {
	private String SHOW_COLUMNS_SQL = "SELECT column_name as field, column_name as comment, data_type as type FROM information_schema.columns where table_name = '%s'";
	
	private final String COLUMN_FIELD = "field";
	private final String COLUMN_TYPE = "type";
	private final String COLUMN_COMMENT = "comment";
	
	@Override
	public void init(DbDto dbDto, String dbUrl) {
		super.init(dbDto, dbUrl);
	}
	
	@Override
	public List<TableDto> listTables(String dbUrl) {
		Assert.notNull(dbUrl);
		
//		NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbId);
//		if (jdbcTemplate == null) {
//			return Lists.newArrayList();
//		}
//		List<Map<String, Object>> tableDatas = jdbcTemplate.queryForList(SHOW_TABLE_SQL, Maps.newHashMap());
//		if (CollectionUtils.isEmpty(tableDatas)) {
//			return Lists.newArrayList();
//		}
//		List<Map<String, Object>> mapDatas = jdbcTemplate.queryForList(SHOW_TABLE_DESC_SQL, Maps.newHashMap());
//		Map<String, String> newMapData = Maps.newHashMap();
//		mapDatas.stream().forEach(mapData -> {
//			newMapData.put(mapData.get(TABLENAME).toString(), Optional.ofNullable(mapData.get(DESCRIPITON)).orElse("").toString());
//		});
//		
//		return tableDatas.stream().map(tableData -> {
//			TableDto tableDto = new TableDto();
//			tableDto.setName(tableData.get(TABLENAME_SOURCE).toString());
//			tableDto.setDesc(newMapData.get(tableData.get(TABLENAME_SOURCE).toString()));
//			return tableDto;
//		}).collect(Collectors.toList());
		return null;
	}

	@Override
	public List<ColumnDto> listColumns(String dbUrl, String tableName) {
		Assert.notNull(dbUrl);
		Assert.notBlank(tableName, "表名");
		
		NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbUrl);
		if (jdbcTemplate == null) {
			return Lists.newArrayList();
		}
		
		String createSql = "";
		String[] tableInfoArr = StringUtils.split(tableName, ".");
		if (tableInfoArr.length == 2) {
			createSql = String.format(SHOW_COLUMNS_SQL+ " and table_schema = '%s'", tableInfoArr[1], tableInfoArr[0]);
		} else {
			createSql = String.format(SHOW_COLUMNS_SQL, tableName);
		}
		
		log.info("query start, createSql: {}", createSql);
		List<Map<String, Object>> mapDatas = jdbcTemplate.queryForList(createSql, Maps.newHashMap());
		log.info("query end, createSql: {}", createSql);
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
