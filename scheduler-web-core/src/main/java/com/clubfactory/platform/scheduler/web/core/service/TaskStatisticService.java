package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.dal.dao.TaskStatisticMapper;
import com.clubfactory.platform.scheduler.dal.po.TaskStatistic;
import com.clubfactory.platform.scheduler.web.core.vo.TaskStatisticVO;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author xiejiajun
 */
@Service("taskStatisticService")
public class TaskStatisticService extends BaseNewService<TaskStatisticVO, TaskStatistic> {

    @Resource
    TaskStatisticMapper taskStatisticMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(taskStatisticMapper);
    }

    @Override
    public PageUtils<TaskStatistic> pageList(TaskStatistic po) {
        return super.pageList(po);
    }
}
