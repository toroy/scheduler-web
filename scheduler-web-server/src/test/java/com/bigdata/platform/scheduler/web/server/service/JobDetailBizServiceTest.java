package com.bigdata.platform.scheduler.web.server.service;

import com.bigdata.platform.scheduler.web.server.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.annotation.Resource;

/**
 * TODO
 *
 * @author zhoulijiang
 * @date 2022/2/28 1:41 下午
 **/
@Slf4j
public class JobDetailBizServiceTest extends BaseTest {

    @Resource
    JobDetailBizService jobDetailBizService;

    @Test
    public void getTableNameTest() {
        String tableName = "coupon_order_record|coupon_order_record_{:d}|256";
        String result = jobDetailBizService.getTableName(tableName);
        log.info(result);
    }
}
