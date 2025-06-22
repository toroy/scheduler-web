package com.zhugeio.platform.scheduler.web.core.dto;

import java.io.Serializable;
import java.util.List;

import com.zhugeio.platform.scheduler.dal.po.CollectDb;
import com.zhugeio.platform.scheduler.web.core.vo.JobVO;

import lombok.Data;

@Data
public class JobDto extends JobVO {

	private static final long serialVersionUID = 6893242054878172860L;

	private List<AlarmDto> alarms;
	
	private Boolean isEdit;
	
	/**
	 * 调度机ip, null 为系统随机
	 */
	private String machineIp;
	
	/**
	 * 脚本名称
	 */
	private String scriptName;
	
	/**
	 * 项目名
	 */
	private String projectName;
	
	private CollectDb sourceDb;
	
	private CollectDb targetDb;
	
	private List<ParamContent> sysParams;

	private List<FileParamsContent> fileParams;

	@Data
	public static class FileParamsContent implements Serializable {

		private String key;

		private Long id;

		private String path;
	}
	
	@Data
	public static class ParamContent implements Serializable {
		
		private String name;
		
		private String value;
	}

	
}
