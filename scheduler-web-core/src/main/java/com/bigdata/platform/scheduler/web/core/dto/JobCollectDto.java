package com.bigdata.platform.scheduler.web.core.dto;

import com.alibaba.fastjson.JSON;
import com.bigdata.platform.scheduler.dal.enums.FormatEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobCollectDto extends JobDto {

	
	private static final long serialVersionUID = 735502115891374803L;
	
	/**
	 * 源表
	 */
	private String sourceTable;
	
	/**
	 * 源id
	 */
	private Long dbSourceId;
	
	/**
	 * 目标表
	 */
	private String targetTable;
	
	/**
	 * 目标源
	 */
	private Long dbTargetId;
	
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
	 * 实时消息配置
	 */
	private MqDto mqDto;
	
	/**
	 * 实例时间
	 */
	private String taskTime = "${task.instance.date.cn}";
	
	/**
	 * 消息id
	 */
	private Long mqId;

	
	@Override
	public String toString() {
		StringBuilder sb = (new StringBuilder("JobCollectDto(sourceTable=")).append(getSourceTable())
				.append(", dbSourceId=").append(getDbSourceId())
				.append(", targetTable=").append(getTargetTable())
				.append(", dbTargetId=").append(getDbTargetId())
				.append(", storageFormat=").append(getStorageFormat())
				.append(", runCount=").append(getRunCount())
				.append(", columnDto=").append(getColumnDto());
		
		return (new StringBuilder("Job(categroy=")).append(getCategroy())
				.append(", type=").append(getType())
				.append(", machineId=").append(getMachineId())
				.append(", name=").append(getName())
				.append(", argsParam=").append(getArgsParam())
				.append(", runOnTmpEmr=").append(getRunOnTmpEmr())
				.append(", priority=").append(getPriority())
				.append(", clusterId=").append(getClusterId())
				.append(", sysParams=").append(JSON.toJSONString(getSysParams()))
				.append(", scriptId=").append(getScriptId())
				.append(", projectId=").append(getProjectId())
				.append(", cycleType=").append(getCycleType())
				.append(", schedulerTime=").append(getSchedulerTime())
				.append(")").append(sb.toString()).toString();
	}
	
}
