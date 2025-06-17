package com.zhugeio.platform.scheduler.web.core.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.zhugeio.platform.scheduler.web.core.dto.JobColumnDto;
import com.zhugeio.platform.scheduler.dal.dto.SubscribeDto;
import com.zhugeio.platform.scheduler.web.core.dto.JobReflueDto;
import com.zhugeio.platform.scheduler.web.core.vo.JobReflueVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.common.util.BeanUtil;
import com.zhugeio.platform.scheduler.dal.dao.JobReflueMapper;
import com.zhugeio.platform.scheduler.dal.po.JobReflue;
import com.google.common.collect.Maps;

@Service
public class JobReflueService extends BaseNewService<JobReflueVO,JobReflue> {

    @Resource
    JobReflueMapper jobReflueMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(jobReflueMapper);
    }

	public JobReflue getByJobId(Long jobId) {
		Assert.notNull(jobId);
		JobReflue jobReflue = new JobReflue();
		jobReflue.setJobId(jobId);
		jobReflue.setIsDeleted(false);
		return this.get(jobReflue);
	}

	public Boolean save(JobReflueDto jobDto, Long jobId, Long localUserId) {
		Assert.notNull(jobDto);
		Assert.notNull(jobId);
		
		JobReflue reflue = new JobReflue();
		BeanUtil.copyBeanNotNull2Bean(jobDto, reflue);
		reflue.setIsSql(jobDto.getColumnDto().getIsSql());
		if (BooleanUtils.isTrue(jobDto.getColumnDto().getIsSql())) {
			reflue.setUserSql(jobDto.getColumnDto().getSql());
			reflue.setSqlColumn(jobDto.getColumnDto().getSqlColumn());
		} else {
			reflue.setSourceColumns(StringUtils.join(jobDto.getColumnDto().getSourceColumns(), ","));
			reflue.setTargetColumns(StringUtils.join(jobDto.getColumnDto().getTargetColumns(), ","));
			reflue.setWhereSql(jobDto.getColumnDto().getWhere());
		}
		reflue.setTargetTable(StringUtils.trim(jobDto.getTargetTable()));
		reflue.setSourceTable(StringUtils.trim(jobDto.getSourceTable()));
		reflue.setSplitPk(jobDto.getColumnDto().getSplitPk());
		reflue.setIncrementType(jobDto.getColumnDto().getIncrementType());
		reflue.setIncrementColumn(jobDto.getColumnDto().getIncrementColumn());
		reflue.setJobId(jobId);
		this.save(reflue);
		return true;
	}

	public Boolean edit(JobReflueDto jobDto, Long localUserId, Boolean isAdmin) {
		Assert.notNull(jobDto);
		
		JobReflue reflue = new JobReflue();
		if (BooleanUtils.isFalse(isAdmin)) {
			reflue.setCreateUser(localUserId);
		}
		reflue.setJobId(jobDto.getId());
		
		Map<String, Object> updateParam = Maps.newHashMap();
		JobColumnDto columnDto = jobDto.getColumnDto();
		if (columnDto != null) {
			updateParam.put("increment_type", columnDto.getIncrementType());
			updateParam.put("where_sql", columnDto.getWhere());
			updateParam.put("split_pk", columnDto.getSplitPk());
			updateParam.put("increment_column", columnDto.getIncrementColumn());
			if (BooleanUtils.isFalse(columnDto.getIsSql())) {
				updateParam.put("source_columns", StringUtils.join(columnDto.getSourceColumns(), ","));
				updateParam.put("target_columns", StringUtils.join(columnDto.getTargetColumns(), ","));
			} else {
				updateParam.put("user_sql", columnDto.getSql());
				updateParam.put("sql_column", columnDto.getSqlColumn());
			}
		}
		updateParam.put("update_user", localUserId);
		updateParam.put("target_table", StringUtils.trim(jobDto.getTargetTable()));
		updateParam.put("db_target_id", jobDto.getDbTargetId());
		updateParam.put("source_table", StringUtils.trim(jobDto.getSourceTable()));
		updateParam.put("db_source_id", jobDto.getDbSourceId());
		updateParam.put("storage_format", jobDto.getStorageFormat());
		updateParam.put("run_count", jobDto.getRunCount());
		reflue.setUpdateParam(updateParam);
		this.edit(reflue);
		return true;
	}

	public Map<Long, List<JobReflue>> getMapByJobIds(List<Long> jobIds) {
		Assert.collectionNotEmpty(jobIds, "jobIds");
		
		JobReflue job = new JobReflue();
		job.setIsDeleted(false);
		job.setIds(jobIds);
		job.setQueryListFieldName("job_id");
		List<JobReflueVO> vos = this.list(job);
		if (CollectionUtils.isEmpty(vos)) {
			return Maps.newHashMap();
		}
		return vos.stream().collect(Collectors.groupingBy(JobReflue::getJobId));
	}
	
	public Map<Long, String> getTargetTableMap(List<Long> jobIds) {
		Assert.collectionNotEmpty(jobIds, "jobIds");
		
		JobReflue job = new JobReflue();
		job.setIsDeleted(false);
		job.setIds(jobIds);
		job.setQueryListFieldName("job_id");
		List<JobReflueVO> vos = this.list(job);
		if (CollectionUtils.isEmpty(vos)) {
			return Maps.newHashMap();
		}
		return vos.stream().collect(Collectors.toMap(JobReflueVO::getJobId, JobReflueVO::getTargetTable));
	}

	public Boolean del(Long jobId) {
		Assert.notNull(jobId);
		
		JobReflue job = new JobReflue();
		job.setJobId(jobId);
		this.logicRemove(job);
		return true;
	}


	public void editOwnerByJobIds(Long userId, Long targetUserId, List<Long> jobIds) {
		Assert.notNull(userId);
		Assert.notNull(targetUserId);
		Assert.collectionNotEmpty(jobIds, "任务id列表");
		
		JobReflue job = new JobReflue();
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
		return this.jobReflueMapper.listSubscribeInfos(jobIds);
	}

}
