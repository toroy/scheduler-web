package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.dal.dao.JobTypeMapper;
import com.zhugeio.platform.scheduler.dal.enums.JobCategoryEnum;
import com.zhugeio.platform.scheduler.dal.po.JobType;
import com.zhugeio.platform.scheduler.web.core.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xiejiajun
 */
@Service
public class JobTypeService {

    @Resource
    private JobTypeMapper jobTypeMapper;


    public JobType get(Long jobTypeId) {
        return jobTypeMapper.select(jobTypeId);
    }

    public Boolean isClusterJob(String jobName, JobCategoryEnum category) {
    	JobType jobType = this.getByName(jobName, category);
    	if (jobType == null) {
    		return false;
    	}
    	return jobType.getIsClusterJob();
    }
    
    public JobType getByName(String jobName, JobCategoryEnum category) {
        return jobTypeMapper.selectByName(jobName, category.name());
    }

    public JobType getByFunction(String function) {
        if (StringUtils.isBlank(function)) {
            return null;
        }
        String[] jobInfo = function.split(Constants.UNDER_LINE);
        if (jobInfo.length != 2) {
            return null;
        }
        JobCategoryEnum category;
        try {
            category = JobCategoryEnum.valueOf(jobInfo[0]);
        } catch (Exception ignore){
            return null;
        }
        return this.getByName(jobInfo[1], category);
    }

    public List<JobType> allType() {
        return jobTypeMapper.list();
    }
}
