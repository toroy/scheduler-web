package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.dao.LogMapMapper;
import com.clubfactory.platform.scheduler.dal.po.LogMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xiejiajun
 */
@Service
public class LogMapService {

    @Resource
    private LogMapMapper logMapMapper;

    public List<LogMap> list(Long taskId){
        Assert.notNull(taskId,"实例ID");
        return logMapMapper.list(taskId);
    }

    public LogMap getById(Long logId){
        Assert.notNull(logId,"日志ID");
        return logMapMapper.select(logId);
    }
}
