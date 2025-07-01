package com.zhugeio.platform.scheduler.web.core.service;

import com.alibaba.fastjson.JSON;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.dao.JobMapper;
import com.zhugeio.platform.scheduler.dal.dto.SchedulerTimeDto;
import com.zhugeio.platform.scheduler.dal.enums.JobCycleTypeEnum;
import com.zhugeio.platform.scheduler.dal.enums.JobStatusEnum;
import com.zhugeio.platform.scheduler.dal.enums.JobTypeEnum;
import com.zhugeio.platform.scheduler.dal.po.Job;
import com.zhugeio.platform.scheduler.web.core.dto.JobDto;
import com.zhugeio.platform.scheduler.web.core.vo.FileParamVO;
import com.zhugeio.platform.scheduler.web.core.vo.JobVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobService extends BaseNewService<JobVO,Job> {

    @Resource
    JobMapper jobMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(jobMapper);
    }
    
    public Map<String, Long> getMapByGroupId(Long groupId) {
    	Assert.notNull(groupId);
    	
    	Job job = new Job();
    	job.setIsDeleted(false);
    	job.setGroupId(groupId);
    	List<JobVO> jobs = this.list(job);
    	if (CollectionUtils.isEmpty(jobs)) {
    		return Maps.newHashMap();
    	}
    	return jobs.stream().collect(Collectors.toMap(JobVO::getName, JobVO::getId, (oldValue, newValue)-> newValue ));
    }
    
    public List<JobVO> listJob() {
    	Job job = new Job();
    	job.setIsDeleted(false);
    	List<JobVO> jobs = this.list(job);
    	return jobs;
    }
    
    public List<Long> listDependJob() {
    	Job job = new Job();
    	job.setIsDeleted(false);
    	job.setQueryListFieldName("cycle_type");
    	job.setIdsString(Lists.newArrayList(JobCycleTypeEnum.DAY.name()
    			, JobCycleTypeEnum.DAYT1.name()));
    	
    	List<JobVO> jobs = this.list(job);
    	if (CollectionUtils.isEmpty(jobs)) {
    		return Lists.newArrayList();
    	}
    	
    	return jobs.stream().map(JobVO::getId).collect(Collectors.toList());
    }

    public void editScheduler(List<Long> ids, SchedulerTimeDto timeDto, JobCycleTypeEnum jobCycleType) {
    	if (CollectionUtils.isEmpty(ids)) {
    		return;
		}
    	Job job = new Job();
    	job.setIds(ids);
    	job.setJobType(JobTypeEnum.DQC);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	job.setUpdateParam(updateParam);
		updateParam.put("scheduler_time", JSON.toJSONString(timeDto));
		updateParam.put("status", JobStatusEnum.CHECK);
		updateParam.put("cycle_type", jobCycleType);
		this.edit(job);
	}

    public void editStatus(Long jobId, JobStatusEnum jobStatus) {
    	Job job = new Job();
    	job.setId(jobId);
    	
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("status", jobStatus);
    	job.setUpdateParam(updateParam);
    	this.edit(job);
    }
    
    public void editStatus(List<Long> jobIds, JobStatusEnum jobStatus, Long userId) {
    	Job job = new Job();
    	job.setIds(jobIds);
    	
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("status", jobStatus);
    	updateParam.put("update_user", userId);
    	job.setUpdateParam(updateParam);
    	this.edit(job);
    }
    
	public Long save(JobDto jobDto, Long userId, String departName) {
		Assert.notNull(jobDto);
		
		Job job = jobDto;
    	job.setCreateUser(userId);
    	job.setDepartName(departName);
    	job.setIsCheckCycle(false);
    	job.setIsRunning(true);
    	job.setUpdateUser(userId);
    	job.setStatus(jobDto.getStatus() != null ? jobDto.getStatus() : JobStatusEnum.DOING);
    	job.setSchedulerTimeDto(jobDto.getSchedulerTimeDto());
    	job.setParams(JSON.toJSONString(jobDto.getSysParams()));
		job.setFileParamsJson(JSON.toJSONString(jobDto.getFileParams()));
    	job.setRunOnTmpEmr(jobDto.getRunOnTmpEmr());
    	if (job.getJobType() == null) {
    		job.setJobType(JobTypeEnum.NORMAL);
		}
    	
    	this.save(job);
    	return job.getId();
	}
	
	public Boolean edit(JobDto jobDto, Long userId, Boolean isAdmin, Boolean isCheck) {
		Assert.notNull(jobDto);
		
		Job job = new Job();
		if (BooleanUtils.isFalse(isAdmin)) {
			job.setCreateUser(userId);
		}
		job.setId(jobDto.getId());
		
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("project_id", jobDto.getProjectId());
    	updateParam.put("update_user", userId);
    	updateParam.put("cluster_id", jobDto.getClusterId());
    	updateParam.put("type", jobDto.getType());
    	updateParam.put("machine_id", jobDto.getMachineId());
    	updateParam.put("args_param", jobDto.getArgsParam());
    	updateParam.put("params", JSON.toJSONString(jobDto.getSysParams()));
		updateParam.put("file_Params_json", JSON.toJSONString(jobDto.getFileParams()));
    	updateParam.put("name", jobDto.getName());
    	updateParam.put("cycle_type", jobDto.getCycleType());
    	updateParam.put("main_class", jobDto.getMainClass());
    	updateParam.put("program_type", jobDto.getProgramType());
    	updateParam.put("deploy_mode", jobDto.getDeployMode());
		updateParam.put("job_conf", jobDto.getJobConf());
    	if (BooleanUtils.isTrue(isCheck)) {
    		updateParam.put("status",  JobStatusEnum.DOING);
    	}
    	updateParam.put("run_on_tmp_emr", jobDto.getRunOnTmpEmr());
    	updateParam.put("version", jobDto.getVersion());
    	updateParam.put("script_id", jobDto.getScriptId());
    	updateParam.put("priority", jobDto.getPriority());
    	updateParam.put("retry_max", jobDto.getRetryMax());
    	updateParam.put("retry_dur", jobDto.getRetryDur());
    	updateParam.put("scheduler_time", JSON.toJSONString(jobDto.getSchedulerTimeDto()));
    	updateParam.put("exec_param", jobDto.getExecParam());
    	job.setUpdateParam(updateParam);
    	this.edit(job);
    	return true;
	}
	
	public Job getById(Long id) {
		Assert.notNull(id);
		
		Job job = new Job();
		job.setId(id);
		job.setIsDeleted(false);
		return this.get(job);
		
	}
	
	/**
	 * 修改脚本，任务重新置为审核中
	 * 
	 * @param scriptId
	 * @param userId
	 * @return
	 */
	public Boolean editRedoingByScriptId(Long scriptId, Long userId) {
		Assert.notNull(scriptId);
		Assert.notNull(userId);
		
		Job job = new Job();
		job.setIsDeleted(false);
		job.setScriptId(scriptId);
		
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("status", JobStatusEnum.DOING);
		updateParam.put("update_user", userId);
		job.setUpdateParam(updateParam);
		this.edit(job);
		return true;
	}

	public Boolean editRedoingByFileParamId(Long fileParamId, Long userId) {
		Assert.notNull(fileParamId);
		Assert.notNull(userId);

		Job job = new Job();
		job.setIsDeleted(false);
		job.setScriptId(fileParamId);

		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("status", JobStatusEnum.DOING);
		updateParam.put("update_user", userId);
		job.setUpdateParam(updateParam);
		this.edit(job);
		return true;
	}
	
	
	public Boolean editPause(List<Long> ids,  Long userId) {
		Assert.collectionNotEmpty(ids, "ids");
		
		Job job = new Job();
		job.setIds(ids);
		job.setIsDeleted(false);
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("status", JobStatusEnum.PAUSE);
		updateParam.put("update_user", userId);
		job.setUpdateParam(updateParam);
		this.edit(job);
		return true;
	}
	
	public Boolean editSuccessByStatus(List<Long> ids,JobStatusEnum status, Long userId) {
		Assert.collectionNotEmpty(ids, "ids");

		Job job = new Job();
		job.setIds(ids);
		job.setIsDeleted(false);
		job.setStatus(status);
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("status", JobStatusEnum.ONLINE);
		updateParam.put("check_user", userId);
		job.setUpdateParam(updateParam);
		this.edit(job);
		return true;
	}
	
	public Boolean editOff(List<Long> ids, Long userId, Boolean isAdmin) {
		Assert.collectionNotEmpty(ids, "任务id列表");
		
		Job job = new Job();
		job.setIds(ids);
		job.setIsDeleted(false);
		if (BooleanUtils.isFalse(isAdmin)) {
			job.setCreateUser(userId);
		}
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("status", JobStatusEnum.OFF);
		job.setUpdateParam(updateParam);
    	this.edit(job);
    	return true;
	}
	
    public Map<Long, Job> getMapByIds(List<Long> ids) {
    	if (CollectionUtils.isEmpty(ids)) {
			return Maps.newHashMap();
		}
    	List<Job> jobs = listByIds(ids);
    	if (CollectionUtils.isEmpty(jobs)) {
			return Maps.newHashMap();
		}
    	Map<Long, Job> mapData = Maps.newHashMap();
    	for (Job job : jobs) {
    		mapData.put(job.getId(), job);
    	}
    	return mapData;
    }
	
	public Boolean del(Long id) {
		Assert.notNull(id);
		
		Job job = new Job();
		job.setId(id);
    	this.logicRemove(job);
    	return true;
	}
	
	public Boolean needCheckCycle(Long id) {
		Assert.notNull(id);
		
		Job job = new Job();
		job.setId(id);
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("is_check_cycle", true);
		job.setUpdateParam(updateParam);
		this.edit(job);
		return true;
	}
	
	public List<Job> listByKey(String key) {
		Assert.notNull(key);
		Job job = new Job();
		job.setIsDeleted(false);
		job.setStatus(JobStatusEnum.ONLINE);
		if (NumberUtils.isCreatable(key)) {
			job.setId(Long.valueOf(key));
		} else {
			job.setName(key.toString());
		}
		return jobMapper.list(job);
	}
	
	public List<Job> listNeedPassByIds(List<Long> reqJobIds) {
		if (CollectionUtils.isEmpty(reqJobIds)) {
			return Lists.newArrayList();
		}
		Job job = new Job();
		job.setIds(reqJobIds);
		job.setIsDeleted(false);
		job.setStatus(JobStatusEnum.CHECK);
		return jobMapper.list(job);
	}
	
	
	public List<Job> listByIdsIfNotDelete(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Lists.newArrayList();
		}
		Job job = new Job();
		job.setIds(ids);
		return jobMapper.list(job);
	}
	
	public List<Job> listByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Lists.newArrayList();
		}
		Job job = new Job();
		job.setIds(ids);
		job.setIsDeleted(false);
		return jobMapper.list(job);
	}
	
	public List<JobVO> listCheckCycle() {
		Job job = new Job();
		job.setIsDeleted(false);
		job.setIsCheckCycle(true);
		return this.list(job);
	}
	
	public List<Long> listCreateUsersByNames(List<String> names) {
		Job job = new Job();
		job.setIsDeleted(false);
		job.setIdsString(names);
		job.setQueryListFieldName("name");
		List<JobVO> jobs = this.list(job);
		if (CollectionUtils.isEmpty(jobs)) {
			return Lists.newArrayList();
		}
		return jobs.stream().map(JobVO::getCreateUser).collect(Collectors.toList());
	}
	
	public List<JobVO> listByCreateUser(Long userId) {
		Assert.notNull(userId);
		Job job = new Job();
		job.setIsDeleted(false);
		job.setCreateUser(userId);
		return this.list(job);
	}
	
	public void editOwnerByJobIds(Long userId, Long targetUserId, List<Long> ids) {
		Assert.notNull(userId);
		Assert.notNull(targetUserId);
		Assert.collectionNotEmpty(ids, "任务id列表");
		
		Job job = new Job();
		job.setIsDeleted(false);
		job.setCreateUser(userId);
		job.setIds(ids);
		Map<String, Object> updateParam = Maps.newHashMap();
		job.setUpdateParam(updateParam);
		updateParam.put("create_user", targetUserId);
		this.edit(job);
	}
	
	public Boolean isExistName(String name, Long id) {
		Assert.notBlank(name);
		
		Job job = new Job();
		job.setIsDeleted(false);
		job.setName(name);
		List<Job> jobs = jobMapper.listByName(job);
		if (CollectionUtils.isEmpty(jobs)) {
			return false;
		}
		if (id == null) {
			return true;
		}
		for (Job jobVO : jobs) {
			if (!jobVO.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	public List<String> listJobNameByMachineId(Long machineId) {
		Assert.notNull(machineId);

		Job job = new Job();
		job.setIsDeleted(false);
		job.setMachineId(machineId);
		List<JobVO> jobs = this.list(job);
		if (CollectionUtils.isEmpty(jobs)) {
			return Lists.newArrayList();
		}
		return jobs.stream().map(JobVO::getName).collect(Collectors.toList());
	}

}
