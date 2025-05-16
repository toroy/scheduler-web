package com.bigdata.platform.scheduler.web.server.vo;

import java.io.Serializable;
import java.util.List;

import com.bigdata.platform.scheduler.web.core.jdbc.dto.ColumnDto;

import lombok.Data;

@Data
public class ColumnsVo implements Serializable {

	private static final long serialVersionUID = -8394243996447532499L;

	private List<ColumnDto> sourceColumns;
	
	private List<ColumnDto> targetColumns;
}
