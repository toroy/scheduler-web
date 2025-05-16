package com.bigdata.platform.scheduler.web.core.dto;

import com.alibaba.fastjson.JSON;
import com.bigdata.platform.scheduler.dal.enums.FormatEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobReflueDto extends JobDto {

	private static final long serialVersionUID = 6952805649204364231L;

	/**
	 * 源表
	 */
	private String sourceTable;
	
	/**
	 * 源id
	 */
	private Long dbSourceId;
	
	/**
	 * 目标id
	 */
	private Long dbTargetId;
	
	/**
	 * 目标id
	 */
	private String targetTable;
	
	/**
	 * 存储格式
	 */
	private FormatEnum storageFormat;
	
	/**
	 * 并发数
	 */
	private Long runCount;
	
	/**
	 * 字段配置
	 */
	private JobColumnDto columnDto;
	
	/**
	 * 实例时间
	 */
	private String taskTime = "${task.instance.date.cn}";
	
	@Override
	public String toString() {
		StringBuilder sb = (new StringBuilder("JobReflueDto(sourceTable=")).append(getSourceTable())
				.append(", dbSourceId=").append(getDbSourceId())
				.append(", dbTargetId=").append(getDbTargetId())
				.append(", targetTable=").append(getTargetTable())
				.append(", storageFormat=").append(getStorageFormat())
				.append(", runCount=").append(getRunCount())
				.append(", columnDto=").append(getColumnDto());

		return (new StringBuilder("Job(categroy=")).append(getCategroy())
				.append(", type=").append(getType())
				.append(", machineId=").append(getMachineId())
				.append(", name=").append(getName())
				.append(", priority=").append(getPriority())
				.append(", runOnTmpEmr=").append(getRunOnTmpEmr())
				.append(", clusterId=").append(getClusterId())
				.append(", argsParam=").append(getArgsParam())
				.append(", sysParams=").append(JSON.toJSONString(getSysParams()))
				.append(", projectId=").append(getProjectId())
				.append(", scriptId=").append(getScriptId())
				.append(", cycleType=").append(getCycleType())
				.append(", schedulerTime=").append(getSchedulerTime())
				.append(")").append(sb.toString()).toString();
	}

}
