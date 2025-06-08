package com.zhugeio.platform.scheduler.web.server.service;

import javax.annotation.Resource;

import com.zhugeio.platform.scheduler.web.server.BaseTest;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.vo.Vertex;
import org.assertj.core.util.Lists;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.zhugeio.platform.scheduler.dal.enums.DependTypeEnum;
import com.zhugeio.platform.scheduler.web.core.dto.GraphDto;
import com.zhugeio.platform.scheduler.web.core.enums.GraphThendType;

public class GraphBizServiceTest extends BaseTest {

	@Resource
	GraphBizService graphBizService;
	
	@Test
	public void checkJobCycleTest() {
		graphBizService.checkCycle();
	}
	
	@Test
	public void getJobOnlineGraphTest() {
		GraphThendType type = GraphThendType.UP;
		Long id = 10004135L;
		Vertex vertex = graphBizService.getJobGraph(type, id, true);
		System.out.println(JSON.toJSONString(vertex, true));
	}
	
	@Test
	public void editGrpahTest() {
		LoginUserDto userDto = new LoginUserDto();
		userDto.setLocalUserId(1L);
		
		GraphDto graphDto = new GraphDto();
		graphDto.setId(248L);
		
		GraphDto parentGraphDto = new GraphDto();
		parentGraphDto.setId(249L);
		parentGraphDto.setType(DependTypeEnum.SAME);
		graphDto.setParents(Lists.newArrayList(parentGraphDto));
		graphBizService.editGrpah(graphDto, userDto);
	}
}
