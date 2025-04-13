package com.clubfactory.platform.scheduler.web.core.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.dao.ColumnMapper;
import com.clubfactory.platform.scheduler.dal.po.Column;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.clubfactory.platform.scheduler.web.core.vo.ColumnVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service
public class ColumnService extends BaseNewService<ColumnVO,Column> {

    @Resource
    ColumnMapper columnMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(columnMapper);
    }

    public Boolean saveBatchColumn(List<ColumnDto> columnDtos, Long foreignId, Long userId) {
    	Assert.notNull(foreignId);
    	Assert.collectionNotEmpty(columnDtos, "字段列表");
    	
    	List<Column> columns = columnDtos.stream().map(columnDto -> {
    		Column column = new Column();
    		BeanUtil.copyBeanNotNull2Bean(columnDto, column);
    		column.setCreateUser(userId);
    		column.setUpdateUser(userId);
    		column.setForeignId(foreignId);
    		return column;
    	}).collect(Collectors.toList());
    	if (CollectionUtils.isNotEmpty(columns)) {
    		this.saveBatch(columns);
    	}
		return true;
    }
    
    public List<ColumnDto> listByForeignId(Long foreignId) {
    	Assert.notNull(foreignId);
    	
    	Column columnReq = new Column();
    	columnReq.setForeignId(foreignId);
    	List<ColumnVO> columns = this.list(columnReq);
    	if (CollectionUtils.isEmpty(columns)) {
    		return Lists.newArrayList();
    	}
    	return columns.stream().map(column -> {
    		ColumnDto columnDto = new ColumnDto();
    		BeanUtil.copyBeanNotNull2Bean(column, columnDto);
    		return columnDto;
    	}).collect(Collectors.toList());
    }
    
    public void editColumnDesc(Long foreignId, String name, String desc) {
    	Assert.notNull(foreignId);
    	Column columnReq = new Column();
    	columnReq.setForeignId(foreignId);
    	columnReq.setName(name);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("`desc`", desc);
    	columnReq.setUpdateParam(updateParam);
    	this.edit(columnReq);
    }
}
