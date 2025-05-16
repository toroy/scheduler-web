package com.bigdata.platform.scheduler.web.server.service;

import com.bigdata.platform.scheduler.web.server.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;

public class DataSourceTest extends BaseTest {

    @Resource
    DataSourceBizService dataSourceBizService;

    @Test
    public void mongodb() {
        dataSourceBizService.testMongodb(null);
    }


}
