package com.zhugeio.platform.scheduler.web.server.service;

import com.zhugeio.platform.scheduler.web.server.BaseTest;
import com.zhugeio.platform.scheduler.web.server.dqc.service.DqcBizService;
import org.junit.Test;

import javax.annotation.Resource;

public class DqcBizServiceTest extends BaseTest {

    @Resource
    DqcBizService dqcBizService;

    @Test
    public void genDqcTaskTest() {
        dqcBizService.genDqcTask();
    }
    

}
