package com.zhugeio.platform.scheduler.web.server.service;

import com.zhugeio.platform.scheduler.web.server.BaseTest;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.basic.TableOnlineLineageBasicService;
import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.web.client.vo.TableLineageGraphVo;
import com.zhugeio.platform.scheduler.web.core.service.TableOnlineLineageService;
import org.junit.Test;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TableLineageTest extends BaseTest {

    @Resource
    TableOnlineLineageBasicService tableOnlineLineageBasicService;

    @Resource
    TableOnlineLineageService tableOnlineLineageService;

    @Resource
    TableLineageBizService tableLineageBizService;


    @Test
    public void getTableLineageGraph() {
        String dbName = "ods_image_phash";
        String dbHost = "ec2-52-13-15-144.us-west-2.compute.amazonaws.com,ec2-54-189-151-179.us-west-2.compute.amazonaws.com,ec2-34-214-78-95.us-west-2.compute.amazonaws.com";
        String tableName = "image_phash_snapdeal";
        BaseResult<TableLineageGraphVo> br = tableOnlineLineageBasicService
                .getTableLineageGraph(dbName, dbHost, tableName);
        System.out.println();
    }

    @Test
    public void genAllJobTableLineage() {
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setLocalUserId(1L);
        tableLineageBizService.genAllJobTableLineage();
    }
    
    
    public static void main(String[] args) {
		Calendar calendar = Calendar.getInstance();
		calendar.get(Calendar.HOUR_OF_DAY);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(dateFormat.format(calendar.getTime()));;
		
	}


}
