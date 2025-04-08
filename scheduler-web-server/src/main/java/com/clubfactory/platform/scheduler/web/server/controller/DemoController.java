package com.clubfactory.platform.scheduler.web.server.controller;

import com.clubfactory.platform.common.bean.BaseResult;
import com.clubfactory.platform.scheduler.web.client.vo.TableLineageGraphVo;
import com.clubfactory.platform.scheduler.web.server.service.DemoBizService;
import com.clubfactory.platform.scheduler.web.server.service.TableLineageBizService;
import com.clubfactory.platform.scheduler.web.server.service.basic.TableOnlineLineageBasicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;

@ApiIgnore
@RestController
@RequestMapping("demo")
public class DemoController {

	@Resource
	DemoBizService demoBizService;
	@Resource
	TableOnlineLineageBasicService tableOnlineLineageBasicService;
	@Resource
	TableLineageBizService tableLineageBizService;

	
	@GetMapping("hello")
	public Boolean list() {
		return demoBizService.get();
	}
	
	@GetMapping("test")
	public Boolean test() {
		demoBizService.test();
		return true;
	}

	@GetMapping("getTableLineageGraph")
	public BaseResult<TableLineageGraphVo> getTableLineageGraph() {
		String db = "dw_dm";
		String dbHost = "ec2-52-13-15-144.us-west-2.compute.amazonaws.com,ec2-54-189-151-179.us-west-2.compute.amazonaws.com,ec2-34-214-78-95.us-west-2.compute.amazonaws.com";
		String tbl = "sale_order_order_df";
		BaseResult<TableLineageGraphVo> br = tableOnlineLineageBasicService
				.getTableLineageGraph(db, dbHost, tbl);
//		BaseResult<TableLineageGraphVo> br = tableOnlineLineageBasicService
//				.getTableLineageGraph(369L);
		return br;
	}


}
