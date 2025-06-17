package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.dao.LogMapMapper;
import com.zhugeio.platform.scheduler.dal.po.LogMap;
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
