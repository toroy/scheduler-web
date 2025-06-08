package com.zhugeio.platform.scheduler.web.server.service;

import javax.annotation.Resource;

import com.zhugeio.platform.scheduler.web.server.BaseTest;
import org.junit.Test;

import com.zhugeio.platform.scheduler.web.core.service.JobOnlineService;

public class JobOnlineServiceTest extends BaseTest {
	
	@Resource
	JobOnlineService jobOnlineService;
	
	@Test
	public void test() {
		jobOnlineService.editVersionBySysSciptId(14L, 2);
	}

}
