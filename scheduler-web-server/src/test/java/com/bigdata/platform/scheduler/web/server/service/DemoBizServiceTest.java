package com.bigdata.platform.scheduler.web.server.service;

import javax.annotation.Resource;

import com.bigdata.platform.scheduler.web.server.BaseTest;
import org.junit.Test;


public class DemoBizServiceTest extends BaseTest {
 
	@Resource
	DemoBizService demoBizService;
//	@Resource
//	HelloService helloService;
//	
//	@Test
//	public void helloTest() {
//		System.out.println(helloService.getHello());
//	}
	
	
	@Test
	public void getTest() {
		System.out.println(demoBizService.get());
	}
	
	@Test
	public void test() {
		demoBizService.test();
	}
}
