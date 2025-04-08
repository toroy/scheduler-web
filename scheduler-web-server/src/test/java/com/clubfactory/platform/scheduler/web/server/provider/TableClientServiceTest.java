package com.clubfactory.platform.scheduler.web.server.provider;

import com.alibaba.fastjson.JSON;
import com.clubfactory.platform.common.bean.BaseResult;
import com.clubfactory.platform.common.bean.PageUtils;
import com.clubfactory.platform.common.bean.Pager;
import com.clubfactory.platform.scheduler.web.client.dto.TableConnectDto;
import com.clubfactory.platform.scheduler.web.client.enums.DbTypeEnum;
import com.clubfactory.platform.scheduler.web.client.service.TableClientService;
import com.clubfactory.platform.scheduler.web.client.vo.TableConnectVo;
import com.clubfactory.platform.scheduler.web.client.vo.TaskVO;
import com.clubfactory.platform.scheduler.web.server.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
public class TableClientServiceTest extends BaseTest {

	@Resource
	TableClientService tableClientService;
	@Resource
	TableClientServiceImpl tableClientServiceImpl;
	
	@Test
	public void listConnectTest() {
		List<DbTypeEnum> types = Lists.newArrayList();
//		types.add(DbTypeEnum.HIVE);
		types.add(DbTypeEnum.MYSQL);
//		types.add(DbTypeEnum.POSTGRESQL);
		BaseResult<List<TableConnectVo>> connectVos = tableClientService.listConnect(types);
		log.info(JSON.toJSONString(connectVos));
	}
	
	@Test
	public void listConnectImplTest() {
		List<DbTypeEnum> types = Lists.newArrayList();
		types.add(DbTypeEnum.HIVE);
//		types.add(DbTypeEnum.MYSQL);
//		types.add(DbTypeEnum.POSTGRESQL);
		BaseResult<List<TableConnectVo>> connectVos = tableClientServiceImpl.listConnect(types);
		log.info(JSON.toJSONString(connectVos));
	}
	
	@Test
	public void listTasks() {
		TableConnectDto dto = new TableConnectDto(); 
		dto.setDbHost("ip-172-31-26-30.us-west-2.compute.internal");
		dto.setDbName("ods_meta");
		dto.setTableName("meta_demo2");
		dto.setDbType(DbTypeEnum.HIVE);
		
		Pager pager = new Pager();
		pager.setPageNo(1);
		pager.setPageSize(20);
		BaseResult<PageUtils<TaskVO>> result = tableClientService.listTasks(dto, pager);
		log.info(JSON.toJSONString(result, true));
	}
}
