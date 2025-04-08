/**
 * File generated at: 2018年11月21日下午4:40:48
 */
package com.clubfactory.platform.scheduler.web.core.jdbc.client;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.TableDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.enums.HiveColumnTypeEnum;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * hive操作类
 * 
 * @author 陈泰（周利江）
 * @Date 2018年11月21日下午4:40:48
 *
 */
@Service
public class HiveOperations extends BaseOperations implements Operations {

	private String SHOW_TABLE_SQL = "show tables";
	private String SHOW_COLUMNS_SQL = "describe %s";
	private String SHOW_FORMATTED_SQL = "describe formatted %s";
	
	private final String COLUMN_DATA_TYPE = "data_type";
	private final String COLUMN_COL_NAME = "col_name";
	private final String COLUMN_COMMENT = "comment";
	private final String TAB_NAME = "tab_name";
	
	@Override
	public void init(DbDto dbDto, String dbUrl) {
		super.init(dbDto, dbUrl);
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
		
		List<ColumnDto> columnDtos = Lists.newArrayList();
		for (Map<String, Object> mapData : mapDatas) {
			if (mapData.get(COLUMN_DATA_TYPE) == null) {
				break;
			}
			ColumnDto columnDto = new ColumnDto();
			columnDto.setName(mapData.get(COLUMN_COL_NAME).toString());
			columnDto.setType(mapData.get(COLUMN_DATA_TYPE).toString());
			columnDto.setDesc(Optional.ofNullable(mapData.get(COLUMN_COMMENT)).orElse("").toString());
			columnDtos.add(columnDto);
		}
		return columnDtos;
	}

	@Override
	public List<ColumnDto> listColumns(String dbUrl, String dbName, String tableName) {
		return listColumns(dbUrl, tableName);
	}

	@Override
	public List<Map<String, Object>> queryForList(String dbUrl, String tableName) {
		Assert.notNull(dbUrl);
		Assert.notBlank(tableName, "表名");
		
		List<Map<String, Object>> dataLists = super.queryForList(dbUrl, tableName);
		return dataLists.stream().map(dataMap -> {
			Map<String, Object> retMap = Maps.newHashMap();
			dataMap.keySet().forEach(key -> {
				Object value = HiveColumnTypeEnum.getDateFormat(dataMap.get(key));
				retMap.put(StringUtils.removeStartIgnoreCase(key,tableName+"."), value);
			});
			return retMap;
		}).collect(Collectors.toList());
		
	}

	@Override
	public List<TableDto> listTables(String dbUrl) {
		Assert.notNull(dbUrl);
		
		NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbUrl);
		if (jdbcTemplate == null) {
			return Lists.newArrayList();
		}
		List<Map<String, Object>> mapDatas = jdbcTemplate.queryForList(SHOW_TABLE_SQL, Maps.newHashMap());
		if (CollectionUtils.isEmpty(mapDatas)) {
			return Lists.newArrayList();
		}
		
		
		List<TableDto> tableDtos = mapDatas.stream().map(mapData -> {
			TableDto dto = new TableDto();
			dto.setName(mapData.get(TAB_NAME).toString());
			return  dto;
			}).collect(Collectors.toList());
		
		tableDtos.stream().forEach(tableDto -> {
			tableDto.setDesc(getTableDesc(tableDto.getName(), jdbcTemplate));
		});
		
		return tableDtos;
	}
	
	private String getTableDesc(String tableName, NamedParameterJdbcTemplate jdbcTemplate) {
		List<Map<String, Object>> mapDatas = jdbcTemplate.queryForList(String.format(SHOW_FORMATTED_SQL, tableName), Maps.newHashMap());
		if (CollectionUtils.isEmpty(mapDatas)) {
			return null;
		}
		for (Map<String, Object> map : mapDatas) {
			if (map.get(COLUMN_DATA_TYPE) != null 
					&& StringUtils.equalsIgnoreCase(map.get(COLUMN_DATA_TYPE).toString().trim(), COLUMN_COMMENT)) {
				//return StringUtil.decodeUnicode(Optional.ofNullable(map.get(COLUMN_COMMENT)).orElse("").toString().trim());
				return null;
			}
		}
		
		return null;
	}

	@Override
	public void close(String dbUrl) {
		Assert.notNull(dbUrl);
		super.close(dbUrl);
	}

}
