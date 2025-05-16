package com.bigdata.platform.scheduler.web.core.dqc.service;

import com.bigdata.platform.scheduler.dal.dao.DqcTaskMapper;
import com.bigdata.platform.scheduler.dal.po.DqcTask;
import com.bigdata.platform.scheduler.web.core.dqc.vo.DqcTaskVO;
import com.bigdata.platform.scheduler.web.core.service.BaseNewService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class DqcTaskService extends BaseNewService<DqcTaskVO, DqcTask> {

    @Resource
    DqcTaskMapper dqcTaskMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(dqcTaskMapper);
    }

}
