package com.clubfactory.platform.scheduler.web.core.dto;

import java.util.List;

import com.clubfactory.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.clubfactory.platform.scheduler.web.core.vo.MqVO;

import lombok.Data;

@Data
public class MqDto extends MqVO {

	private static final long serialVersionUID = 94031176641025906L;

	/**
	 * 是否可以编辑
	 */
	private Boolean isEdit;
	
	/**
	 * 是否创建topic
	 */
	private Boolean isCreateTopic;
	
	/**
	 * 是否创建表
	 */
	private Boolean isCreateTable;
	
	private List<ColumnDto> columns;
}
