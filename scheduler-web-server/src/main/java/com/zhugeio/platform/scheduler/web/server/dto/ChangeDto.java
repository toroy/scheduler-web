package com.zhugeio.platform.scheduler.web.server.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ChangeDto implements Serializable {

	private static final long serialVersionUID = -4036103565868949723L;

	/**
	 * 原管理员id
	 */
	private Long originOwnerId;
	
	/**
	 * 目标管理员id
	 */
	private Long targetOwnerId;
	
	/**
	 * 是否移交全部任务
	 */
	private Boolean isJob;
	
	/**
	 * 移交的任务列表，不传就是移交所有任务
	 */
	private List<Long> jobIds;
	
	/**
	 * 是否移交全部脚本
	 */
	private Boolean isScript;
	
	/**
	 * 移交的脚本类别，不传就是移交所有脚本
	 */
	private List<Long> scriptIds;
	
	/**
	 * 是否移交全部项目
	 */
	private Boolean isProject;
	
	/**
	 * 移交项目id列表，不传移交所有项目
	 */
	private List<Long> projectIds;
}
