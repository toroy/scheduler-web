package com.clubfactory.platform.scheduler.web.server.service;

import com.clubfactory.platform.scheduler.common.enums.ResourceType;
import com.clubfactory.platform.scheduler.web.core.service.LockService;
import com.clubfactory.platform.scheduler.web.server.dqc.service.DqcBizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class CrontabBizService {
	
	@Resource
	GraphBizService graphBizService;
	@Resource
	DqcBizService dqcBizService;
	@Resource
	LockService lockService;

	//@Scheduled(cron ="*/30 * * * * ?")
	public void checkJobCycle() {
		log.info("--------------- 开始检查循环依赖 ----------------");
		synchronized (this) {
			graphBizService.checkCycle();
		}
		
	}

	@Scheduled(cron ="0 15 * * * ?")
	public void genDqcTask() {
		if (lockService.tryLockOnce(ResourceType.DQC_TASK_LOCK)) {
			log.info("--------------- 生成dqc实例列表 ----------------");
			try {
				dqcBizService.genDqcTask();
			} catch (Exception e) {
				log.error("生成dqc实例列表 error", e);
			} finally {
				lockService.unlock(ResourceType.DQC_TASK_LOCK);
			}
		}

	}
}
