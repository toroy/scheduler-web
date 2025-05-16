package com.bigdata.platform.scheduler.web.server.config;

import com.bigdata.platform.scheduler.web.core.utils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author xiejiajun
 */
@Configuration
public class CommonPropertiesConfig {

    @Autowired
    private CommonProperties commonProperties;


    @PostConstruct
    public void initPropertyUtils(){
        PropertyUtils.init(commonProperties.getProperties());
    }
}
