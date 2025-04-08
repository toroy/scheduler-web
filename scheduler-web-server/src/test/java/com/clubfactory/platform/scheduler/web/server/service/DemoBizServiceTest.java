package com.clubfactory.platform.scheduler.web.server.service;

import javax.annotation.Resource;

import org.junit.Test;

import com.clubfactory.platform.scheduler.web.server.BaseTest;


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
