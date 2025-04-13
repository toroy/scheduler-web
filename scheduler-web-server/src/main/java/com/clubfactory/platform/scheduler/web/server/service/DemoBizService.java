package com.clubfactory.platform.scheduler.web.server.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.dal.po.Demo;
import com.clubfactory.platform.scheduler.web.core.service.DemoService;
import com.clubfactory.platform.scheduler.web.core.vo.DemoVO;

@Service
public class DemoBizService {

	@Resource
	DemoService demoService;
	
	public Boolean get() {
		Demo demo = new Demo();
		demo.setIsDeleted(false);
		DemoVO demoVO = demoService.get(demo);
		return demoVO.getIsDeleted();
	}
	
    @Transactional
	public void test() {
    	Demo demo = new Demo();
    	demo.setName("test");
    	demoService.save(demo);
    	
    	if (true) {
    		throw new BizException("test");
    	}
    	
    	
    	demoService.save(demo);
	}
}
