package com.bigdata.platform.scheduler.web.server.service;

import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.dal.dao.TaskMonitorMapper;
import com.bigdata.platform.scheduler.dal.po.TaskMonitor;
import com.bigdata.platform.scheduler.web.core.service.BaseNewService;
import com.bigdata.platform.scheduler.web.server.vo.TaskMonitorVO;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class TaskMonitorService extends BaseNewService<TaskMonitorVO, TaskMonitor> {

    @Resource
    TaskMonitorMapper taskMonitorMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(taskMonitorMapper);
    }

    /**
     * 根据taskId获取Monitor信息
     * @param taskId
     * @return
     */
    public TaskMonitor getByTaskId(Long taskId){
        Assert.notNull(taskId);

        TaskMonitor taskMonitor = new TaskMonitor();
        taskMonitor.setTaskId(taskId);
        return this.get(taskMonitor);
    }

}
