package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.dao.JobOnlineDependsMapper;
import com.zhugeio.platform.scheduler.dal.enums.DependTypeEnum;
import com.zhugeio.platform.scheduler.dal.po.JobOnlineDepends;
import com.zhugeio.platform.scheduler.web.core.vo.JobOnlineDependsVO;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobOnlineDependsService extends BaseNewService<JobOnlineDependsVO,JobOnlineDepends> {

    @Resource
    JobOnlineDependsMapper jobOnlineDependsMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(jobOnlineDependsMapper);
    }

    public List<JobOnlineDependsVO> listByJobId(Long id) {
    	Assert.notNull(id);
    	
    	JobOnlineDepends job = new JobOnlineDepends();
    	job.setIsDeleted(false);
    	job.setJobId(id);
    	return this.list(job);
    }

	public List<JobOnlineDependsVO> list() {
		JobOnlineDepends job = new JobOnlineDepends();
		job.setIsDeleted(false);
		return this.list(job);
	}

	public List<JobOnlineDependsVO> listNoSelf() {
		JobOnlineDepends job = new JobOnlineDepends();
		job.setIsDeleted(false);
		return this.list(job).stream().filter(dep -> !DependTypeEnum.SELF.equals(dep.getType())).collect(Collectors.toList());
	}

    public void saveBatchByParentId(List<Long> jobIds, Long parentId) {
    	Assert.collectionNonEmpty(jobIds, "jobIds");
    	Assert.notNull(parentId);

		List<JobOnlineDepends> depends = jobIds.stream().map(id -> {
			JobOnlineDepends job = new JobOnlineDepends();
			job.setParentId(parentId);
			job.setId(id);
			job.setIsDeleted(false);
			return job;
		}).collect(Collectors.toList());
		this.saveBatch(depends);
	}

	public void saveBatchByJobId(List<Long> jobIds, List<Long> parentIds) {
		Assert.collectionNonEmpty(jobIds, "jobIds");
		Assert.collectionNonEmpty(parentIds, "parentIds");

		List<JobOnlineDepends> depends = Lists.newArrayList();
		for (Long jobId : jobIds) {
			for (Long parentId : parentIds) {
				JobOnlineDepends job = new JobOnlineDepends();
				job.setParentId(parentId);
				job.setId(jobId);
				job.setIsDeleted(false);
				depends.add(job);
			}
		}
		this.saveBatch(depends);
	}

	public List<Long> listJobIdsByParentId(Long id) {
    	return this.listByParentId(id).stream().map(JobOnlineDependsVO::getJobId).collect(Collectors.toList());
	}
    
    public List<JobOnlineDependsVO> listByParentId(Long id) {
    	Assert.notNull(id);
    	
    	JobOnlineDepends job = new JobOnlineDepends();
    	job.setIsDeleted(false);
    	job.setParentId(id);
    	return this.list(job);
    }

    public Boolean removeByJobId(Long jobId) {
    	Assert.notNull(jobId);
    	JobOnlineDepends job = new JobOnlineDepends();
    	job.setJobId(jobId);
    	this.remove(job);
    	return true;
    }

	public Boolean removeByJobIds(List<Long> jobIds) {
		Assert.collectionNonEmpty(jobIds, "jobIds");
		JobOnlineDepends job = new JobOnlineDepends();
		job.setIds(jobIds);
		job.setQueryListFieldName("job_id");
		this.remove(job);
		return true;
	}

	public Boolean removeByParentJobIds(List<Long> jobIds) {
		Assert.collectionNonEmpty(jobIds, "jobIds");
		JobOnlineDepends job = new JobOnlineDepends();
		job.setIds(jobIds);
		job.setQueryListFieldName("parent_id");
		this.remove(job);
		return true;
	}

    public Boolean remove(List<Long> jobIds) {
    	Assert.collectionNotEmpty(jobIds, "任务id列表");
    	JobOnlineDepends job = new JobOnlineDepends();
    	job.setIds(jobIds);
    	job.setQueryListFieldName("job_id");
    	this.remove(job);
    	return true;
    }
    
    public List<JobOnlineDependsVO> listNoSelfByParentIds(List<Long> ids) {
    	Assert.collectionNotEmpty(ids, "任务列表");
    	
    	JobOnlineDepends job = new JobOnlineDepends();
    	job.setIsDeleted(false);
    	job.setIds(ids);
    	job.setQueryListFieldName("parent_id");
    	List<JobOnlineDependsVO> dependsVOs = this.list(job);
    	if (CollectionUtils.isEmpty(dependsVOs)) {
    		return Lists.newArrayList();
    	}
    	return dependsVOs.stream().filter(dep -> !DependTypeEnum.SELF.equals(dep.getType())).collect(Collectors.toList());
    }

}
