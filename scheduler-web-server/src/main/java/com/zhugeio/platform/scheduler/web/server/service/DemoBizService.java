package com.zhugeio.platform.scheduler.web.server.service;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.dal.po.Demo;
import com.zhugeio.platform.scheduler.web.core.service.DemoService;
import com.zhugeio.platform.scheduler.web.core.vo.DemoVO;

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
		System.out.println(JSON.toJSONString(demo));
	}
}
