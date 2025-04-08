package com.clubfactory.platform.scheduler.web.server.service;

import javax.annotation.Resource;

import org.junit.Test;

import com.clubfactory.platform.scheduler.web.core.service.JobOnlineService;
import com.clubfactory.platform.scheduler.web.server.BaseTest;

public class JobOnlineServiceTest extends BaseTest {
	
	@Resource
	JobOnlineService jobOnlineService;
	
	@Test
	public void test() {
		jobOnlineService.editVersionBySysSciptId(14L, 2);
	}

}
