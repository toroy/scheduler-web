package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.dao.JobCalMapper;
import com.clubfactory.platform.scheduler.dal.po.JobCal;
import com.clubfactory.platform.scheduler.web.core.dto.JobCalDto;
import com.clubfactory.platform.scheduler.web.core.vo.JobCalVO;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobCalService extends BaseNewService<JobCalVO,JobCal> {

    @Resource
    JobCalMapper jobCalMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(jobCalMapper);
    }

	public Boolean save(JobCalDto jobDto, Long jobId, Long localUserId) {
		Assert.notNull(jobDto);
		Assert.notNull(jobId);
		
		JobCal cal = new JobCal();
		cal.setTargetTable(StringUtils.trim(jobDto.getTargetTable()));
		cal.setDbTargetId(jobDto.getDbTargetId());
		cal.setJobId(jobDto.getId());
		cal.setCreateUser(localUserId);
		cal.setUpdateUser(localUserId);
		cal.setJobId(jobId);
		this.save(cal);
		return true;
	}
	
	public Boolean edit(JobCalDto jobDto, Long localUserId, Boolean isAdmin) {
		Assert.notNull(jobDto);
		
		JobCal cal = new JobCal();
		cal.setJobId(jobDto.getId());
		if (BooleanUtils.isFalse(isAdmin)) {
			cal.setCreateUser(localUserId);
		}
		
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("target_table", StringUtils.trim(jobDto.getTargetTable()));
		updateParam.put("db_target_id", jobDto.getDbTargetId());
		updateParam.put("update_user", localUserId);
		cal.setUpdateParam(updateParam);
		this.edit(cal);
		return true;
	}
	
	public Boolean del(Long jobId) {
		Assert.notNull(jobId);
		
		JobCal cal = new JobCal();
		cal.setJobId(jobId);
		
		this.logicRemove(cal);
		return true;
	}
	
	public JobCal getByJobId(Long jobId) {
		Assert.notNull(jobId);
		
		JobCal cal = new JobCal();
		cal.setJobId(jobId);
		cal.setIsDeleted(false);
		return this.get(cal);
	}
	
	public Map<Long, List<JobCal>> getMapByJobIds(List<Long> jobIds) {
		Assert.collectionNotEmpty(jobIds, "jobIds");

		List<JobCalVO> vos = listByJobIds(jobIds);
		if (CollectionUtils.isEmpty(vos)) {
			return Maps.newHashMap();
		}
		return vos.stream().collect(Collectors.groupingBy(JobCal::getJobId));
	}

	public Map<Long, Long> getMapDbTargetId(List<Long> jobIds) {
		List<JobCalVO> calVOS = this.listByJobIds(jobIds);
		if (CollectionUtils.isEmpty(calVOS)) {
			return Maps.newHashMap();
		}
		return calVOS.stream().collect(Collectors.toMap(JobCalVO::getJobId, JobCalVO::getDbTargetId, (oldValue, newValue) -> newValue));
	}

	private List<JobCalVO> listByJobIds(List<Long> jobIds) {
		JobCal job = new JobCal();
		job.setIsDeleted(false);
		job.setIds(jobIds);
		job.setQueryListFieldName("job_id");
		return this.list(job);
	}

	public Map<Long, String> getTargetTableMap(List<Long> jobIds) {
		Assert.collectionNotEmpty(jobIds, "jobIds");
		
		JobCal job = new JobCal();
		job.setIsDeleted(false);
		job.setIds(jobIds);
		job.setQueryListFieldName("job_id");
		List<JobCalVO> vos = this.list(job);
		if (CollectionUtils.isEmpty(vos)) {
			return Maps.newHashMap();
		}
		return vos.stream().collect(Collectors.toMap(JobCal::getJobId, JobCal::getTargetTable));
	}

	public void editOwnerByJobIds(Long userId, Long targetUserId, List<Long> jobIds) {
		Assert.notNull(userId);
		Assert.notNull(targetUserId);
		Assert.collectionNotEmpty(jobIds, "任务id列表");
		
		JobCal job = new JobCal();
		job.setIsDeleted(false);
		job.setCreateUser(userId);
		job.setIds(jobIds);
		job.setQueryListFieldName("job_id");
		Map<String, Object> updateParam = Maps.newHashMap();
		job.setUpdateParam(updateParam);
		updateParam.put("create_user", targetUserId);
		this.edit(job);
	}
	

}
