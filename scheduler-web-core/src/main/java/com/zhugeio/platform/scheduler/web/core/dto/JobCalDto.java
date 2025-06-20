package com.zhugeio.platform.scheduler.web.core.dto;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobCalDto extends JobDto {

	private static final long serialVersionUID = -1680347242781713579L;

	
	/**
	 * 目标表
	 */
	private String targetTable;
	
	/**
	 * 目标DB源名称
	 */
	private String dbTargetName;
	
	/**
	 * 源id
	 */
	private Long dbTargetId;
	
	@Override
	public String toString() {
		StringBuilder sb = (new StringBuilder("JobCalDto(targetTable=")).append(getTargetTable()).append(", dbTargetId=")
				.append(getDbTargetId()).append(")");
		
		return (new StringBuilder("Job(categroy=")).append(getCategroy())
				.append(", type=").append(getType())
				.append(", machineId=").append(getMachineId())
				.append(", name=").append(getName())
				.append(", runOnTmpEmr=").append(getRunOnTmpEmr())
				.append(", priority=").append(getPriority())
				.append(", clusterId=").append(getClusterId())
				.append(", argsParam=").append(getArgsParam())
				.append(", sysParams=").append(JSON.toJSONString(getSysParams()))
				.append(", fileParams=").append(getFileParams())
				.append(", scriptId=").append(getScriptId())
				.append(", projectId=").append(getProjectId())
				.append(", cycleType=").append(getCycleType())
				.append(", schedulerTime=").append(getSchedulerTime())
				.append(")").append(sb.toString()).toString();
	}
	
}


