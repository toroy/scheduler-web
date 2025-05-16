package com.bigdata.platform.scheduler.web.server.dto;

import java.io.Serializable;

import com.bigdata.platform.scheduler.dal.enums.DeployModeEnum;
import com.bigdata.platform.scheduler.dal.enums.ProgramTypeEnum;

import lombok.Data;

@Data
public class ExecParamDto implements Serializable  {

	private static final long serialVersionUID = 4122739729833517265L;

	
	private String mainArgs;
	
	private String params;
	
	
	/**
	 * java jvm 参数
	 */
	private String jvmParam;
	
	/**
	 * 系统参数
	 */
	private String sysConfigs;
	
	/**
	 * 类型
	 */
	private ProgramTypeEnum languageType;
	
	/**
	 * 主函数
	 */
	private String mainClass;
	
	/**
	 * 部署模式
	 */
	private String deployMode;

}
