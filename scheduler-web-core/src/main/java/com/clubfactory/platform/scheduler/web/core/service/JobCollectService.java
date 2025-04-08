package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.common.bean.tuple.Tuple2;
import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.dao.JobCollectMapper;
import com.clubfactory.platform.scheduler.dal.dto.SubscribeDto;
import com.clubfactory.platform.scheduler.dal.enums.IncrementTypeEnum;
import com.clubfactory.platform.scheduler.dal.po.JobCollect;
import com.clubfactory.platform.scheduler.web.core.dto.JobCollectDto;
import com.clubfactory.platform.scheduler.web.core.dto.JobColumnDto;
import com.clubfactory.platform.scheduler.web.core.vo.JobCollectVO;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobCollectService extends BaseNewService<JobCollectVO,JobCollect> {

    @Resource
    JobCollectMapper jobCollectMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(jobCollectMapper);
    }

	public JobCollect getByJobId(Long jobId) {
		Assert.notNull(jobId);
		
		JobCollect collect = new JobCollect();
		collect.setJobId(jobId);
		collect.setIsDeleted(false);
		return this.get(collect);
	}
	
	public Tuple2<Boolean, Long> isExistTargetTable(Long targetId, String targetTable, Long jobId) {
		Assert.notBlank(targetTable, "targetTable");
		
		JobCollect collect = new JobCollect();
		collect.setIsDeleted(false);
		collect.setDbTargetId(targetId);
		collect.setTargetTable(targetTable);
		List<JobCollectVO> collects = this.list(collect);
		
		if (CollectionUtils.isEmpty(collects)) {
			return new Tuple2<>(false, null);
		}
		
		if (jobId != null) {
			Iterator<JobCollectVO> iterator = collects.iterator();
			while (iterator.hasNext()) {
				JobCollectVO jobCollectVO = iterator.next();
				if (jobId.equals(jobCollectVO.getJobId())) {
					iterator.remove();
				}
			}
			
			if (CollectionUtils.isEmpty(collects)) {
				return new Tuple2<>(false, null);
			}
		}
		Long targetJobId = collects.stream().filter(dto -> dto.getJobId() != null).map(JobCollectVO::getJobId).findFirst().orElse(0L);
		return new Tuple2<>(true, targetJobId);

	}

	public void save(JobCollectDto jobDto, Long jobId, Long localUserId) {
		Assert.notNull(jobDto);
		Assert.notNull(jobDto.getColumnDto());
		
		JobCollect collect = new JobCollect();
		BeanUtil.copyBeanNotNull2Bean(jobDto, collect);
		collect.setSplitPk(jobDto.getColumnDto().getSplitPk());
		if (CollectionUtils.isNotEmpty(jobDto.getColumnDto().getTargetColumns())) {
			collect.setTargetColumns(StringUtils.join(jobDto.getColumnDto().getTargetColumns(), ","));
		}
		if (CollectionUtils.isNotEmpty(jobDto.getColumnDto().getSourceColumns())) {
			collect.setSourceColumns(StringUtils.join(jobDto.getColumnDto().getSourceColumns(), ","));
		}
		collect.setTargetTable(StringUtils.trim(jobDto.getTargetTable()));
		collect.setSourceTable(StringUtils.trim(jobDto.getSourceTable()));
		collect.setIncrementColumn(jobDto.getColumnDto().getIncrementColumn());
		collect.setIncrementType(jobDto.getColumnDto().getIncrementType());
		collect.setWhereSql(jobDto.getColumnDto().getWhere());
		collect.setCreateUser(localUserId);
		collect.setUpdateUser(localUserId);
		collect.setJobId(jobId);
		collect.setStorageFormat(jobDto.getStorageFormat());
		this.save(collect);
	}

	public Boolean edit(JobCollectDto jobDto, Long localUserId, Boolean isAdmin) {
		Assert.notNull(jobDto);
		
		JobCollect collect = new JobCollect();
		if (BooleanUtils.isFalse(isAdmin)) {
			collect.setCreateUser(localUserId);
		}
		collect.setJobId(jobDto.getId());
		
		Map<String, Object> updateParam = Maps.newHashMap();
		JobColumnDto columnDto = jobDto.getColumnDto();
		if (columnDto != null) {
			if (CollectionUtils.isNotEmpty(columnDto.getTargetColumns())) {
				updateParam.put("target_columns", StringUtils.join(columnDto.getTargetColumns(),","));
			}
			if (CollectionUtils.isNotEmpty(columnDto.getSourceColumns())) {
				updateParam.put("source_columns", StringUtils.join(columnDto.getSourceColumns(),","));
			}
			updateParam.put("increment_type", columnDto.getIncrementType());
			updateParam.put("where_sql", columnDto.getWhere());
			updateParam.put("split_pk", columnDto.getSplitPk());
			if (columnDto.getIncrementType() == IncrementTypeEnum.ALL) {
				updateParam.put("increment_column", "");
			} else {
				updateParam.put("increment_column", columnDto.getIncrementColumn());
			}
		}
		updateParam.put("update_user", localUserId);
		updateParam.put("target_table", StringUtils.trim(jobDto.getTargetTable()));
		updateParam.put("db_target_id", jobDto.getDbTargetId());
		updateParam.put("source_table", StringUtils.trim(jobDto.getSourceTable()));
		updateParam.put("db_source_id", jobDto.getDbSourceId());
		updateParam.put("storage_format", jobDto.getStorageFormat());
		updateParam.put("run_count", jobDto.getRunCount());
		collect.setUpdateParam(updateParam);
		this.edit(collect);
		return true;
	}
	
	public void editSourceByJobId(Long jobId, String sourceName, String targetName, Long dbId) {
		Assert.notNull(jobId);
		Assert.notBlank(sourceName);
		
		JobCollect job = new JobCollect();
		job.setJobId(jobId);
		
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("source_table", sourceName);
		updateParam.put("target_table", targetName);
		updateParam.put("db_source_id", dbId);
		job.setUpdateParam(updateParam);
		this.edit(job);
	}
	
	
	public Map<Long, List<JobCollect>> getMapByJobIds(List<Long> jobIds) {
		Assert.collectionNotEmpty(jobIds, "jobIds");
		
		JobCollect job = new JobCollect();
		job.setIsDeleted(false);
		job.setIds(jobIds);
		job.setQueryListFieldName("job_id");
		List<JobCollectVO> vos = this.list(job);
		if (CollectionUtils.isEmpty(vos)) {
			return Maps.newHashMap();
		}
		return vos.stream().collect(Collectors.groupingBy(JobCollect::getJobId));
	}
	
	public Map<Long, String> getTargetTableMap(List<Long> jobIds) {
		Assert.collectionNotEmpty(jobIds, "jobIds");
		
		JobCollect job = new JobCollect();
		job.setIsDeleted(false);
		job.setIds(jobIds);
		job.setQueryListFieldName("job_id");
		List<JobCollectVO> vos = this.list(job);
		if (CollectionUtils.isEmpty(vos)) {
			return Maps.newHashMap();
		}
		return vos.stream().collect(Collectors.toMap(JobCollect::getJobId, JobCollect::getTargetTable));
	}

	public Boolean del(Long jobId) {
		Assert.notNull(jobId);
		
		JobCollect job = new JobCollect();
		job.setJobId(jobId);
		this.logicRemove(job);
		return true;
	}

	public void editOwnerByJobIds(Long userId, Long targetUserId, List<Long> jobIds) {
		Assert.notNull(userId);
		Assert.notNull(targetUserId);
		Assert.collectionNotEmpty(jobIds, "任务id列表");
		
		JobCollect job = new JobCollect();
		job.setIsDeleted(false);
		job.setCreateUser(userId);
		job.setIds(jobIds);
		job.setQueryListFieldName("job_id");
		Map<String, Object> updateParam = Maps.newHashMap();
		job.setUpdateParam(updateParam);
		updateParam.put("create_user", targetUserId);
		this.edit(job);
	}

	/**
	 * 根据父依赖ID列表拉取依赖订阅所需信息
	 * @param jobIds
	 * @return
	 */
	public List<SubscribeDto> listSubscribeInfosByJobId(List<Long> jobIds) {
		return this.jobCollectMapper.listSubscribeInfos(jobIds);
	}
}
