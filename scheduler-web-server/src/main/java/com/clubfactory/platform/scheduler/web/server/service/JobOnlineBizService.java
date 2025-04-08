package com.clubfactory.platform.scheduler.web.server.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.clubfactory.platform.scheduler.web.core.service.JobOnlineService;

@Service
public class JobOnlineBizService {

	@Resource
	JobOnlineService jobonlineService;
}
