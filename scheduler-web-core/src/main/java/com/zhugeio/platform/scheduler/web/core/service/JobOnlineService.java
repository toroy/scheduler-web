package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.common.bean.tuple.Tuple2;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.dao.JobOnlineMapper;
import com.zhugeio.platform.scheduler.dal.enums.JobCategoryEnum;
import com.zhugeio.platform.scheduler.dal.enums.JobStatusEnum;
import com.zhugeio.platform.scheduler.dal.enums.JobTypeEnum;
import com.zhugeio.platform.scheduler.dal.po.JobOnline;
import com.zhugeio.platform.scheduler.web.core.vo.JobOnlineVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhoulijiang
 */
@Service
public class JobOnlineService extends BaseNewService<JobOnlineVO,JobOnline> {

    @Resource
    JobOnlineMapper jobOnlineMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(jobOnlineMapper);
    }
    
    public Boolean editVersionBySysSciptId(Long scriptId, Integer version) {
    	Assert.notNull(scriptId);
    	Assert.notNull(version);
    	
    	JobOnline job = new JobOnline();
    	job.setIsDeleted(false);
    	job.setScriptId(scriptId);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("version", version);
    	job.setUpdateParam(updateParam);
    	
    	this.edit(job);
    	return true;
    }

    public List<JobOnlineVO> listByBizDb() {
		JobOnline job = new JobOnline();
		job.setIsDeleted(false);
		List<String> categorys = Lists.newArrayList();
		categorys.add(JobCategoryEnum.COLLECT.name());
		categorys.add(JobCategoryEnum.REFLUE.name());
		job.setIdsString(categorys);
		job.setQueryListFieldName("categroy");
		return this.list(job);
	}

	public List<JobOnline> list() {
		JobOnline jobOnline = new JobOnline();
		jobOnline.setIsDeleted(false);
		return jobOnlineMapper.list(jobOnline);
	}
    
    public Map<String, List<JobOnline>> getMapByNames(List<String> names) {
    	Assert.collectionNotEmpty(names, "任务名");
    	
    	JobOnline job = new JobOnline();
    	job.setIsDeleted(false);
    	job.setIdsString(names);
    	job.setQueryListFieldName("name");
    	List<JobOnline> jobOnlines = jobOnlineMapper.list(job);
    	if (CollectionUtils.isEmpty(jobOnlines)) {
    		return Maps.newHashMap();
    	}
    	return jobOnlines.stream().collect(Collectors.groupingBy(JobOnline::getName));
    }
    
    public Map<Long, JobOnline> getMapByIds(List<Long> ids) {
    	if (CollectionUtils.isEmpty(ids)) {
			return Maps.newHashMap();
		}
    	List<JobOnline> jobs = listByIds(ids);
    	if (CollectionUtils.isEmpty(jobs)) {
			return Maps.newHashMap();
		}
    	Map<Long, JobOnline> mapData = Maps.newHashMap();
    	for (JobOnline job : jobs) {
    		mapData.put(job.getJobId(), job);
    	}
    	return mapData;
    }

    public Map<Long, Integer> getVersionMapByJobIds(List<Long> ids) {
    	return this.listByIds(ids).stream().collect(Collectors.toMap(JobOnline::getJobId, JobOnline::getVersion, (oldValue, newValue) -> newValue));
	}

	public List<JobOnline> listByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Lists.newArrayList();
		}
		JobOnline job = new JobOnline();
		job.setIds(ids);
		job.setIsDeleted(false);
		job.setQueryListFieldName("job_id");
		return jobOnlineMapper.list(job);
	}
    
    
	public List<JobOnline> listByIdsIfNotDelete(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Lists.newArrayList();
		}
		JobOnline job = new JobOnline();
		job.setIds(ids);
		job.setIsDeleted(false);
		job.setJobType(JobTypeEnum.NORMAL);
		job.setQueryListFieldName("job_id");
		return jobOnlineMapper.list(job);
	}


	public Map<Long, String> getTargetMap(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return Maps.newHashMap();
		}
		List<JobOnline> jobOnlines = this.listByIdsIfNotDelete(ids);
		return jobOnlines.stream().collect(Collectors.toMap(JobOnline::getJobId, JobOnline::getTargetTable));
		
	}
	
	public Boolean deleteByJobIds(List<Long> jobIds) {
		Assert.collectionNotEmpty(jobIds, "jobIds");
		JobOnline job = new JobOnline();
		job.setIds(jobIds);
		job.setQueryListFieldName("job_id");
		this.logicRemove(job);
		return true;
	}
	
	public Boolean editSuccess(List<Long> ids,  Long userId) {
		Assert.collectionNotEmpty(ids, "ids");
		
		JobOnline job = new JobOnline();
		job.setIds(ids);
		job.setIsDeleted(false);
		job.setQueryListFieldName("job_id");
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("status", JobStatusEnum.ONLINE);
		updateParam.put("update_user", userId);
		job.setUpdateParam(updateParam);
		this.edit(job);
		return true;
	}
	
	public Boolean editRetryByJobId(Long jobId, Integer retryMax, Integer retryDur) {
		Assert.notNull(jobId);
		if (retryMax == null || retryDur == null) {
			return true;
		}
		JobOnline job = new JobOnline();
		job.setJobId(jobId);
		job.setIsDeleted(false);
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("retry_max", retryMax);
		updateParam.put("retry_dur", retryDur);
		job.setUpdateParam(updateParam);
		this.edit(job);
		return true;
		
	}
	
	public Boolean editPause(List<Long> ids,  Long userId) {
		Assert.collectionNotEmpty(ids, "ids");
		
		JobOnline job = new JobOnline();
		job.setIds(ids);
		job.setIsDeleted(false);
		job.setQueryListFieldName("job_id");
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("status", JobStatusEnum.PAUSE);
		updateParam.put("update_user", userId);
		job.setUpdateParam(updateParam);
		this.edit(job);
		return true;
	}
	
	public JobOnline getById(Long id) {
		Assert.notNull(id);
		
		JobOnline job = new JobOnline();
		job.setJobId(id);
		job.setIsDeleted(false);
		return this.get(job);
		
	}
	
	public List<JobOnlineVO> listByKey(String key) {
		Assert.notNull(key);
		JobOnline job = new JobOnline();
		job.setIsDeleted(false);
		if (NumberUtils.isCreatable(key)) {
			job.setJobId(Long.valueOf(key));
		} else {
			job.setName(key.toString());
		}
		return this.list(job);
	}
	
	public Map<String, Long> getMapByCreateUser(Long userId) {
		Assert.notNull(userId);
		JobOnline job = new JobOnline();
		job.setIsDeleted(false);
		job.setCreateUser(userId);
		return this.list(job).stream().collect(Collectors.toMap(JobOnline::getName, JobOnline::getJobId, (oldValue, newValue) -> newValue));
	}
	
	/**
	 * 获取线上任务元组信息
	 * 
	 * @param groupId 组id
	 * @return List<Long> 任务id列表
	 *         Map<Long, Long> key: 任务id
	 *                         value: 用户id
	 */
	public Tuple2<List<Long>, Map<Long, Long>> getTuple2(Long groupId) {
		Assert.notNull(groupId);
		
		JobOnline job = new JobOnline();
		job.setIsDeleted(false);
		job.setGroupId(groupId);
		List<JobOnlineVO> jobOnlineVOs = this.list(job);
		Map<Long, Long> map = jobOnlineVOs.stream().collect(Collectors.toMap(JobOnline::getJobId, JobOnline::getCreateUser, (oldValue, newValue) -> newValue));
		List<Long> jobIds = jobOnlineVOs.stream().map(JobOnline::getJobId).collect(Collectors.toList());
		return new Tuple2<List<Long>, Map<Long, Long>>(jobIds, map);
	}

	/**
	 * 获取线上任务元组信息
	 *
	 * @param reqJobIds
	 * @return
	 */
	public Tuple2<List<Long>, Map<Long, Long>> getTuple2(List<Long> reqJobIds) {
		Assert.collectionNotEmpty(reqJobIds, "任务id列表");
		
		JobOnline job = new JobOnline();
		job.setIsDeleted(false);
		job.setIds(reqJobIds);
		job.setQueryListFieldName("job_id");
		List<JobOnlineVO> jobOnlineVOs = this.list(job);
		Map<Long, Long> map = jobOnlineVOs.stream().collect(Collectors.toMap(JobOnline::getJobId, JobOnline::getCreateUser, (oldValue, newValue) -> newValue));
		List<Long> jobIds = jobOnlineVOs.stream().map(JobOnline::getJobId).collect(Collectors.toList());
		return new Tuple2<List<Long>, Map<Long, Long>>(jobIds, map);
	}
	
	public static void main(String[] args) {
		List<JobOnline> vos = Lists.newArrayList();
		vos.stream().map(JobOnline::getJobId).collect(Collectors.toList());
	}

	public void editOwnerByJobIds(Long userId, Long targetUserId, List<Long> ids) {
		Assert.notNull(userId);
		Assert.notNull(targetUserId);
		Assert.collectionNotEmpty(ids, "任务id列表");
		
		JobOnline job = new JobOnline();
		job.setIsDeleted(false);
		job.setCreateUser(userId);
		job.setIds(ids);
		job.setQueryListFieldName("job_id");
		Map<String, Object> updateParam = Maps.newHashMap();
		job.setUpdateParam(updateParam);
		updateParam.put("create_user", targetUserId);
		this.edit(job);
	}

	public List<String> listJobNameByMachineId(Long machineId) {
		Assert.notNull(machineId);

		JobOnline job = new JobOnline();
		job.setIsDeleted(false);
		job.setMachineId(machineId);
		List<JobOnlineVO> jobs = this.list(job);
		if (CollectionUtils.isEmpty(jobs)) {
			return Lists.newArrayList();
		}
		return jobs.stream().map(JobOnlineVO::getName).collect(Collectors.toList());
	}

}
