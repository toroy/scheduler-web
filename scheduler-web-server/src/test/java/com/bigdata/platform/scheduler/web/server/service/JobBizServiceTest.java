package com.bigdata.platform.scheduler.web.server.service;

import com.bigdata.platform.scheduler.web.server.BaseTest;
import com.bigdata.platform.scheduler.web.server.dto.ChangeDto;
import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.dal.enums.FormatEnum;
import com.bigdata.platform.scheduler.dal.enums.IncrementTypeEnum;
import com.bigdata.platform.scheduler.web.core.dto.JobColumnDto;
import com.bigdata.platform.scheduler.web.core.dto.JobReflueDto;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.junit.Test;

import javax.annotation.Resource;

public class JobBizServiceTest extends BaseTest {

	@Resource
	JobBizService jobBizService;
	
	@Test
	public void execParamTest() {
		JobReflueDto jobDto = new JobReflueDto();
		jobDto.setId(1L);
		jobDto.setSourceTable("source_table");
		jobDto.setTargetTable("target_table");
		jobDto.setDbSourceId(1L);
		jobDto.setDbTargetId(1L);
		jobDto.setScriptId(1L);
		jobDto.setParams("a=1,c=2");
		jobDto.setStorageFormat(FormatEnum.TEXTFILE);
		jobDto.setRunCount(3L);
		
		JobColumnDto columnDto = new JobColumnDto();
		columnDto.setIncrementColumn("pt");
		columnDto.setIncrementType(IncrementTypeEnum.ADD);
		columnDto.setSourceColumns(Lists.newArrayList("a","b"));
		columnDto.setTargetColumns(Lists.newArrayList("a","b"));
		columnDto.setIsSql(true);
		columnDto.setSplitPk("pt");
		columnDto.setSqlColumn("a,b");
		columnDto.setWhere("a=1");
		columnDto.setSql("select * from test");
		jobDto.setColumnDto(columnDto);
		
//		jobBizService.genReflueExecParam(jobDto);
//		System.out.println(JSON.toJSONString(jobDto));
		
	}
	
	@Test
	public void changeOwnerTest() {
		ChangeDto changeDto = new ChangeDto();
		changeDto.setIsJob(true);
		changeDto.setJobIds(Lists.newArrayList(110L));
		changeDto.setOriginOwnerId(5L);
		changeDto.setTargetOwnerId(2L);
		
		
		LoginUserDto userDto = new LoginUserDto();
		userDto.setIsAdmin(true);
		jobBizService.changeOwner(changeDto, userDto);
	}

}
