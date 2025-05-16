package com.bigdata.platform.scheduler.web.core.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.dal.dao.JobDependsMapper;
import com.bigdata.platform.scheduler.dal.enums.DependTypeEnum;
import com.bigdata.platform.scheduler.dal.po.JobDepends;
import com.bigdata.platform.scheduler.web.core.vo.JobDependsVO;
import com.google.common.collect.Lists;

@Service
public class JobDependsService extends BaseNewService<JobDependsVO,JobDepends> {

    @Resource
    JobDependsMapper jobDependsMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(jobDependsMapper);
    }
    
    public List<JobDependsVO> listJobDepends() {
    	JobDepends job = new JobDepends();
    	job.setIsDeleted(false);
    	List<JobDependsVO> jobs = this.list(job);
    	return jobs;
    }

    public List<JobDependsVO> listNoSelfByParentIds(List<Long> ids) {
    	Assert.collectionNotEmpty(ids, "任务列表");
    	
    	JobDepends job = new JobDepends();
    	job.setIsDeleted(false);
    	job.setIds(ids);
    	job.setQueryListFieldName("parent_id");
    	List<JobDependsVO> dependsVOs = this.list(job);
    	if (CollectionUtils.isEmpty(dependsVOs)) {
    		return Lists.newArrayList();
    	}
    	return dependsVOs.stream().filter(dep -> !DependTypeEnum.SELF.equals(dep.getType())).collect(Collectors.toList());
    }
    
    public List<JobDependsVO> listByJobId(Long id) {
    	Assert.notNull(id);
    	
    	JobDepends job = new JobDepends();
    	job.setIsDeleted(false);
    	job.setJobId(id);
    	return this.list(job);
    }
    
    public List<JobDependsVO> listByParentId(Long id) {
    	Assert.notNull(id);
    	
    	JobDepends job = new JobDepends();
    	job.setIsDeleted(false);
    	job.setParentId(id);
    	return this.list(job);
    }
    
    public List<JobDependsVO> listByParentIds(List<Long> ids) {
    	Assert.collectionNotEmpty(ids, "父节点列表");
    	
    	JobDepends job = new JobDepends();
    	job.setIsDeleted(false);
    	job.setIds(ids);
    	job.setQueryListFieldName("parent_id");
    	return this.list(job);
    }
    
    public Boolean isExistSelfDepends(Long jobId) {
    	Assert.notNull(jobId);
    	JobDepends job = new JobDepends();
    	job.setType(DependTypeEnum.SELF);
    	job.setJobId(jobId);
    	job.setParentId(jobId);
    	JobDepends jobVO = this.get(job);
    	if (jobVO != null) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public void removeSelfDepends(Long jobId) {
    	Assert.notNull(jobId);
    	JobDepends job = new JobDepends();
    	job.setType(DependTypeEnum.SELF);
    	job.setJobId(jobId);
    	job.setParentId(jobId);
    	this.remove(job);
    }
    
    public void addSelfDepends(Long jobId, Long userId) {
    	Assert.notNull(jobId);
    	JobDepends job = new JobDepends();
    	job.setType(DependTypeEnum.SELF);
    	job.setJobId(jobId);
    	job.setParentId(jobId);
    	job.setCreateUser(userId);
    	job.setUpdateUser(userId);
    	this.save(job);
    }
    
    public Boolean remove(List<Long> ids) {
    	Assert.collectionNotEmpty(ids, "id列表");
    	JobDepends job = new JobDepends();
    	job.setQueryListFieldName("job_id");
    	job.setIds(ids);
    	this.remove(job);
    	return true;
    }
}
