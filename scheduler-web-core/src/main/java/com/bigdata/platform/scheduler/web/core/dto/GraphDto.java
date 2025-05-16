package com.bigdata.platform.scheduler.web.core.dto;

import java.io.Serializable;
import java.util.List;

import com.bigdata.platform.scheduler.dal.enums.DependTypeEnum;

import lombok.Data;

@Data
public class GraphDto implements Serializable {

	private static final long serialVersionUID = -4160846816577536902L;

	/**
	 * 当前节点
	 */
	private Long id;
	
	/**
	 * 任务名称，与id 二选一
	 */
	private String name;
	
	/**
	 * 是否自依赖
	 */
	private Boolean isSelfDependent;
	
	/**
	 * 是否线上
	 */
	private Boolean isOnline;
	
	/**
	 * 依赖类型
	 */
	private DependTypeEnum type;
	
	/**
	 * 父节点列表
	 */
	private List<GraphDto> parents;
	
	/**
	 * 子节点列表
	 */
	private List<GraphDto> childs;
	
}
