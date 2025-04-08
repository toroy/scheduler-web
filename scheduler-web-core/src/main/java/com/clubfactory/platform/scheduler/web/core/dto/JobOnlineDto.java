package com.clubfactory.platform.scheduler.web.core.dto;

import java.util.List;

import com.clubfactory.platform.scheduler.dal.po.CollectDb;
import com.clubfactory.platform.scheduler.dal.po.JobOnline;
import com.clubfactory.platform.scheduler.web.core.dto.JobDto.ParamContent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobOnlineDto extends JobOnline {

	private static final long serialVersionUID = 4245648698919810865L;

	private List<ParamContent> sysParams;
	
	private List<AlarmDto> alarms;
	
	/**
	 * 脚本名称
	 */
	private String scriptName;
	
	private CollectDb sourceDb;
	
	private CollectDb targetDb;
	
	/**
	 * 字段配置
	 */
	private JobColumnDto columnDto;

}
