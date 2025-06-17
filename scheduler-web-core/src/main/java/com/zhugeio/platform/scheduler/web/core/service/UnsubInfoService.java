package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.web.core.vo.UnsubInfoVO;
import com.zhugeio.platform.scheduler.dal.dao.UnsubInfoMapper;
import com.zhugeio.platform.scheduler.dal.po.UnsubInfo;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class UnsubInfoService extends BaseNewService<UnsubInfoVO, UnsubInfo> {

    @Resource
    UnsubInfoMapper unsubInfoMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(unsubInfoMapper);
    }

}
