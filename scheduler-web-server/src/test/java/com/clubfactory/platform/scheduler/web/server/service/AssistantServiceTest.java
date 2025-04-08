package com.clubfactory.platform.scheduler.web.server.service;

import com.clubfactory.platform.scheduler.web.server.service.basic.AssistantBasicService;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.Test;

public class AssistantServiceTest {//extends BaseTest {


    @Test
    public void getGetObjectRequestFrom() {
        AssistantBasicService abs = new AssistantBasicService();
        MutableObject<String> fileName = new MutableObject<>();
        abs.getGetObjectRequestFrom("s3://cfdp/tmp/scheduler-pre/assitant_download/batch_script_temp.tar", fileName);
    }

}
