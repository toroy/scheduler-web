package com.bigdata.platform.scheduler.web.server.service;

import org.junit.Test;

public class TableTest {

    // test_sms_send_record|test_sms_send_record_{dt}|%Y%m|1
    @Test
    public void test1() {
        JobDetailBizService jd = new JobDetailBizService();
        String str = "sms_send_record|sms_send_record_{dt}|%Y%m|1";
//        String str = "sale_order_line|sale_order_line_{:02x}|256";
//        System.out.println(jd.getTableName(str));
    }

}
