package com.clubfactory.platform.scheduler.web.core.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.clubfactory.platform.scheduler.dal.dao.DemoMapper;
import com.clubfactory.platform.scheduler.dal.po.Demo;
import com.clubfactory.platform.scheduler.web.core.vo.DemoVO;

@Service
public class DemoService extends BaseNewService<DemoVO,Demo> {

    @Resource
    DemoMapper demoMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(demoMapper);
    }

}
