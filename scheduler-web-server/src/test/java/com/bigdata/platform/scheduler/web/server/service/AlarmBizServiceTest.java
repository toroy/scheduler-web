package com.bigdata.platform.scheduler.web.server.service;

import com.bigdata.platform.scheduler.web.server.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;

public class AlarmBizServiceTest extends BaseTest {

    @Resource
    AlarmBizService alarmBizService;

    @Test
    public void runTest() {
        alarmBizService.run();
    }
}
