
package com.clubfactory.platform.scheduler.web.server.service;

import com.alibaba.fastjson.JSON;
import com.clubfactory.platform.scheduler.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.common.constant.DateFormatPattern;
import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.common.util.DateUtil;
import com.clubfactory.platform.scheduler.dal.dto.SchedulerTimeDto;
import com.clubfactory.platform.scheduler.dal.dto.TaskTimeDto;
import com.clubfactory.platform.scheduler.dal.enums.*;
import com.clubfactory.platform.scheduler.dal.po.*;
import com.clubfactory.platform.scheduler.web.core.Constants;
import com.clubfactory.platform.scheduler.web.core.constant.Fields;
import com.clubfactory.platform.scheduler.web.core.dto.JobExtCommonDto;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.enums.JobPageType;
import com.clubfactory.platform.scheduler.web.core.service.*;
import com.clubfactory.platform.scheduler.web.core.utils.CommonUtils;
import com.clubfactory.platform.scheduler.web.core.utils.JobTypeCache;
import com.clubfactory.platform.scheduler.web.core.utils.StringUtil;
import com.clubfactory.platform.scheduler.web.core.vo.*;
import com.clubfactory.platform.scheduler.web.server.constant.JobConstant;
import com.clubfactory.platform.scheduler.web.server.dqc.service.DqcRuleBizService;
import com.clubfactory.platform.scheduler.web.server.dto.AddTaskDto;
import com.clubfactory.platform.scheduler.web.server.dto.ChangeDto;
import com.clubfactory.platform.scheduler.web.server.dto.JobQueryDto;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.service.inter.JobPageCallback;
import com.clubfactory.platform.scheduler.web.server.vo.JobEnumVo;
import com.clubfactory.platform.scheduler.web.server.vo.JobEnumVo.Content;
import com.clubfactory.platform.scheduler.web.server.vo.JobQueryVo;
import com.clubfactory.platform.scheduler.web.server.vo.VertexBase;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobBizService {

	@Resource
	MachineService machineService;
	@Resource
	CollectDbService collectDbService;
	@Resource
	AlarmService alarmService;
	@Resource
	JobService jobService;
	@Resource
	JobCalService jobCalService;
	@Resource
	JobDependsService jobDependsService;
	@Resource
	JobCollectService jobCollectService;
	@Resource
	JobReflueService jobReflueService;
	@Resource
	TaskService taskService;
	@Resource
	TaskDependsService taskDependsService;
	@Resource
	UserService userService;
	@Resource
	JobOnlineService jobOnlineService;
	@Resource
	JobOnlineDependsService jobOnlineDependsService;
	@Resource
	ClusterService clusterService;
	@Resource
	ScriptService scriptService;
	@Resource
	ParamService paramService;
	@Resource
	TaskDependsBizService taskDependsBizService;
	@Resource
	ProjectService projectService;
	@Resource
	JobTypeService jobTypeService;
	@Resource
	DqcRuleBizService dqcRuleBizService;
	@Resource
	UserInfoService userInfoService;
	@Resource
	TableLineageBizService tableLineageBizService;

	private static final int TASK_LIMIT = 128;

	/**
	 * 暂停任务，只有管理员可以操作
	 * @param ids
	 * @param userDto
	 * @return
	 */
	@Transactional
	public Boolean pause(List<Long> ids, LoginUserDto userDto) {
		if (BooleanUtils.isFalse(userDto.getIsAdmin())) {
			throw new BizException(ErrorCode.NOT_ADMIN_ERROR);
		}
		
		List<JobOnline> jobs = jobOnlineService.listByIds(ids);
		for (JobOnline job : jobs) {
			if (JobStatusEnum.ONLINE != job.getStatus()) {
				throw new BizException(ErrorCode.JOB_NOT_ALLOW_PAUSE.setParams(job.getId().toString()));
			}
		}
		
		Long userId = userDto.getLocalUserId();
		jobService.editPause(ids, userId);
		jobOnlineService.editPause(ids, userId);
		taskService.editStopByJobIds(ids, userId);
		return true;
	}
	



	public void resume(List<Long> ids, LoginUserDto userDto) {
		Assert.collectionNotEmpty(ids, "任务id列表");
		
		if (BooleanUtils.isFalse(userDto.getIsAdmin())) {
			throw new BizException(ErrorCode.NOT_ADMIN_ERROR);
		}
		
		List<JobOnline> jobs = jobOnlineService.listByIds(ids);
		for (JobOnline job : jobs) {
			if (JobStatusEnum.PAUSE != job.getStatus()) {
				throw new BizException(ErrorCode.JOB_NOT_ALLOW_RESUME.setParams(job.getId().toString()));
			}
		}

		// 改暂停的场景
		jobOnlineService.editSuccess(ids, userDto.getLocalUserId());
		jobService.editSuccessByStatus(ids, JobStatusEnum.PAUSE, userDto.getLocalUserId());
		// 对应task全部恢复
		//taskService.regainByIds(ids);
	}
	
	public void editCheck(Long jobId) {
		Assert.notNull(jobId);
		jobService.editStatus(jobId, JobStatusEnum.DOING);
	}

	


	public PageUtils<JobQueryVo> queryCheckByPage(JobQueryDto queryDto, LoginUserDto userDto) {
		queryDto.setIsOnline(false);
		return this.queryBasePage(queryDto, userDto, new JobPageCallback() {

			@Override
			public PageUtils<BaseJob> doInPageList(BaseJob baseJob) {
				baseJob.setOrderBy("status asc, update_time desc");
				Job job = new Job();
				BeanUtil.copyBeanNotNull2Bean(baseJob, job);
				job.setStatus(JobStatusEnum.CHECK);
				PageUtils<Job> vos = jobService.pageList(job);
				return new PageUtils(vos.getRows(), vos.getTotalCount(), vos.getPageSize(), vos.getPageNo());
			}

		});
	}

	
	@Transactional
	public Boolean delete(List<Long> ids, LoginUserDto userDto) {
		Assert.collectionNotEmpty(ids, "任务id");
		
		for (Long id : ids) {
			deleteById(userDto, id);
		}
		return true;
	}

	private void deleteById(LoginUserDto userDto, Long id) {
		Job job = jobService.getById(id);
		if (job == null) {
			throw new BizException(ErrorCode.JOB_NOT_EXISTE);
		}
		if (!job.getCreateUser().equals(userDto.getLocalUserId())
				&& userDto.getIsAdmin().equals(false)) {
			throw new BizException(ErrorCode.JOB_NOT_PERMISSION);
		}
		JobOnline jobOnline =jobOnlineService.getById(id);
		if (jobOnline != null) {
			throw new BizException(ErrorCode.JOB_NOT_ALLOW_DELETE_ERROR);
		}
		
		// 删除线下任务
		jobService.del(id);
		if (JobCategoryEnum.CAL == job.getCategroy()) {
			jobCalService.del(id);
		} else if (JobCategoryEnum.COLLECT == job.getCategroy()) {
			jobCollectService.del(id);
		} else if (JobCategoryEnum.REFLUE == job.getCategroy()) {
			jobReflueService.del(id);
		}
		
		// 删除实例
		taskService.delByJobId(id);
	}


	public PageUtils<JobQueryVo> queryPage(JobQueryDto queryDto, LoginUserDto userDto) {
		queryDto.setIsOnline(false);
		PageUtils<JobQueryVo> pageUtils = this.queryBasePage(queryDto, userDto, new JobPageCallback() {

			@Override
			public PageUtils<BaseJob> doInPageList(BaseJob baseJob) {
				baseJob.setOrderBy("update_time desc, create_time desc");
				
				Job job = new Job();
				BeanUtil.copyBeanNotNull2Bean(baseJob, job);
				PageUtils<Job> vos = jobService.pageList(job);
				return new PageUtils(vos.getRows(), vos.getTotalCount(), vos.getPageSize(), vos.getPageNo());
			}
		});
		
		setIsAllowDel(pageUtils, userDto);
		
		return pageUtils;
	}

	private void setIsAllowDel(PageUtils<JobQueryVo> pageUtils, LoginUserDto userDto) {
		List<JobQueryVo> queryVos = pageUtils.getRows();
		if (CollectionUtils.isEmpty(queryVos)) {
			return;
		}
		List<Long> jobIds = queryVos.stream().map(JobQueryVo::getId).collect(Collectors.toList());
		List<Long> jobOnlineIds = jobOnlineService.listByIds(jobIds).stream().map(JobOnline::getJobId).distinct().collect(Collectors.toList());
		for (JobQueryVo jobQueryVo : queryVos) {
			if (jobOnlineIds.contains(jobQueryVo.getId())) {
				jobQueryVo.setIsAllowDel(false);
			} else {
				jobQueryVo.setIsAllowDel(true);
			}
		}
		
		// 线上无任务状态（下线，或从未上线状态）可以删除
		for (JobQueryVo jobQueryVo : queryVos) {
			if (jobOnlineIds.contains(jobQueryVo.getId())) {
				jobQueryVo.setIsAllowDel(false);
			} else {
				jobQueryVo.setIsAllowDel(true);
			}
		}
		
		// 非本人或者管理员，不能删除
		for (JobQueryVo jobQueryVo : queryVos) {
			if (!jobQueryVo.getCreateUser().equals(userDto.getLocalUserId())
					&& userDto.getIsAdmin().equals(false)) {
				jobQueryVo.setIsAllowDel(false);
			} 
		}
	}
	
	public PageUtils<JobQueryVo> queryOnlinePage(JobQueryDto queryDto, LoginUserDto userDto) {
		queryDto.setIsOnline(true);
		return this.queryBasePage(queryDto, userDto, new JobPageCallback() {

			@Override
			public PageUtils<BaseJob> doInPageList(BaseJob baseJob) {
				baseJob.setOrderBy("update_time desc, create_time desc");
				JobOnline jobOnline = new JobOnline();
				BeanUtil.copyBeanNotNull2Bean(baseJob, jobOnline);
				jobOnline.setJobId(baseJob.getId());
				jobOnline.setId(null);
				PageUtils<JobOnline> vos = jobOnlineService.pageList(jobOnline);
				List<JobOnline> jobOnlines = vos.getRows().stream().map(vo -> {  
						vo.setId(vo.getJobId());
						return vo;
					}).collect(Collectors.toList());
				return new PageUtils(jobOnlines, vos.getTotalCount(), vos.getPageSize(), vos.getPageNo());
			}
		});
	}
	
	

	private PageUtils<JobQueryVo> queryBasePage(JobQueryDto queryDto, LoginUserDto userDto, JobPageCallback iJobBizService) {
		Assert.notNull(queryDto);
		Assert.notNull(queryDto.getPageNo());
		Assert.notNull(queryDto.getPageSize());
		// 查询数据
		BaseJob job = getBaseJob(queryDto);
		if (job == null) {
			return new PageUtils(Lists.newArrayList(), 0, queryDto.getPageSize(), queryDto.getPageNo());
		}
		PageUtils<BaseJob> pageVo = iJobBizService.doInPageList(job);

		// 返回数据
		if (pageVo.getSize() == 0) {
			return new PageUtils(queryDto.getPageSize(), queryDto.getPageNo());
		}
		Boolean isOnline = queryDto.getIsOnline();
		List<JobQueryVo> jobQueryVos = listJobQueryVos(userDto, pageVo, isOnline);
		return new PageUtils(jobQueryVos, pageVo.getTotalCount(), queryDto.getPageSize(), queryDto.getPageNo());
	}

	@NotNull
	private BaseJob getBaseJob(JobQueryDto queryDto) {
		BaseJob job = new BaseJob();
		job.setStartDate(queryDto.getStartTime());
		job.setEndDate(DateUtil.getAfterDay(queryDto.getEndTime(), DateFormatPattern.YYYY_MM_DD, 1));
		job.setId(queryDto.getId());
		job.setName(queryDto.getName());
		job.setType(queryDto.getType());
		job.setStatus(queryDto.getStatus());
		job.setCycleType(queryDto.getCycleType());
		job.setProjectId(queryDto.getProjectId());
		job.setCategroy(queryDto.getCategory());
		job.setPageNo(queryDto.getPageNo());
		job.setPageSize(queryDto.getPageSize());
		job.setStatuses(queryDto.getStatuses());
		job.setJobType(JobTypeEnum.NORMAL);
		job.setRunOnTmpEmr(queryDto.getRunOnTmpEmr());
		job.setTargetTable(queryDto.getTargetTable());
		job.setIsDeleted(false);
		if (queryDto.getPriority() != null) {
			job.setPriority(queryDto.getPriority().getCode());
		}
		if (StringUtils.isNotBlank(queryDto.getUserName()) || StringUtils.isNotBlank(queryDto.getDepartName())) {
			List<Long> userIds = userService.listIdsByUserNameAndDepartName(queryDto.getUserName(), queryDto.getDepartName());
			if (CollectionUtils.isEmpty(userIds)) {
				return null;
			}
			job.setIds(userIds);
			job.setQueryListFieldName("create_user");
		}
		return job;
	}

	@NotNull
	private List<JobQueryVo> listJobQueryVos(LoginUserDto userDto, PageUtils<BaseJob> pageVo, Boolean isOnline) {
		Map<Long, String> projectNameMap = projectService.getNameMap();
		Map<Long, String> machineNameMap = machineService.getNameMap();
		Map<Long, Script> idScriptMap = getIdScriptMap(pageVo);
		return pageVo.getRows().stream().map(vo -> {
				JobQueryVo jobQueryVo = new JobQueryVo();
				jobQueryVo.setCategory(vo.getCategroy());
				jobQueryVo.setType(vo.getType());
				jobQueryVo.setId(vo.getId());
				jobQueryVo.setIsOnline(isOnline);
				jobQueryVo.setStatus(vo.getStatus());
				jobQueryVo.setIsEdit(isOnline ? false : true);
				jobQueryVo.setCreateUser(vo.getCreateUser());
				jobQueryVo.setUserName(userService.getUserName(vo.getCreateUser()));
				jobQueryVo.setDepartName(userService.getDepartName(vo.getCreateUser()));
				if (vo.getCheckUser() != null) {
					jobQueryVo.setCheckUserName(userService.getUserName(vo.getCheckUser()));
				}
				jobQueryVo.setCycleTypeStr(CommonUtils.getEnumDesc(vo.getCycleType()));
				jobQueryVo.setName(vo.getName());
				jobQueryVo.setTargetTable(vo.getTargetTable());
				jobQueryVo.setRetryMax(vo.getRetryMax());
				// 计算|HIVE
				JobType jobType = JobTypeCache.getJobTypeByFunction(
						String.format("%s%s%s", vo.getCategroy().name(), Constants.UNDER_LINE, vo.getType()));
				jobQueryVo.setTypeDesc(jobType == null ? "" : jobType.getType());
				jobQueryVo.setCreateTimeStr(DateUtil.format(vo.getCreateTime(), DateFormatPattern.YYYY_MM_DD_HH_MM_SS));
				jobQueryVo.setStatusDesc(CommonUtils.getEnumDesc(vo.getStatus()));
				if (vo.getPriority() != null) {
					jobQueryVo.setPriorityDesc(PriorityEnum.getByCode(vo.getPriority()).getDesc());
				}
				jobQueryVo.setScriptId(vo.getScriptId());
				Script script = idScriptMap.get(vo.getScriptId());
				// 线上直接数据里获取版本号
				if (isOnline) {
					jobQueryVo.setScriptVersion(vo.getVersion());
				} else {
					if (script != null) {
						jobQueryVo.setScriptVersion(script.getVersion());
					}
				}
				jobQueryVo.setIdIndex(vo.getId()+vo.getStatus().name());
				jobQueryVo.setSchedulerTimeStr(vo.getCycleType().getSchedulerTime(vo.getSchedulerTimeDto()));
				jobQueryVo.setMachineName(machineNameMap.get(vo.getMachineId()));
				jobQueryVo.setProjectName(projectNameMap.get(vo.getProjectId()));
				if (vo.getCreateUser().equals(userDto.getLocalUserId()) || BooleanUtils.isTrue(userDto.getIsAdmin())) {
					// 线上的不能配依赖
					if (isOnline) {
						jobQueryVo.setIsAllowDepend(false);
					} else {
						jobQueryVo.setIsAllowDepend(true);
					}
					jobQueryVo.setIsAllowRepair(true);
				} else {
					jobQueryVo.setIsAllowDepend(false);
					jobQueryVo.setIsAllowRepair(false);
				}

				return jobQueryVo;
			}).collect(Collectors.toList());
	}

	private Map<Long, Script> getIdScriptMap(PageUtils<BaseJob> pageVo) {
		List<Long> ids = pageVo.getRows().stream().map(vo -> vo.getScriptId()).collect(Collectors.toList());
		Map<Long, Script> idScriptMap = new HashMap<>();
		List<Script> scripts =  scriptService.listPoByField(Fields.ID, ids);
		for (Script script : scripts) {
			idScriptMap.put(script.getId(), script);
		}
		return idScriptMap;
	}


	

	/**
	 * 下线
	 * 检查此节点下是否有子节点，如果有，不删，报错
	 * 逻辑删sc_job_online,sc_job改为已下线，物理删sc_job_depends,sc_job_online_depends,逻辑删alarm。逻辑删已初始化的task
	 *
	 * @param jobIds
	 * @param userDto
	 * @return
	 */
	@Transactional
	public Boolean disable(List<Long> jobIds, LoginUserDto userDto) {
		Assert.collectionNotEmpty(jobIds, "任务id");
		Long userId = userDto.getLocalUserId();
		Boolean isAdmin = userDto.getIsAdmin();
		
		checkPermission(jobIds, userDto);
		
		List<JobOnlineDependsVO> dependsVOs = jobOnlineDependsService.listNoSelfByParentIds(jobIds);
		if (CollectionUtils.isNotEmpty(dependsVOs)) {
			throw new BizException(ErrorCode.NOT_LEAF_NODE_ERROR);
		}
		// 线上job操作
		jobOnlineDependsService.remove(jobIds);
		jobOnlineService.deleteByJobIds(jobIds);

		//dqc下线
		List<Long> dqcJobIds = dqcRuleBizService.offRelJob(jobIds);
		if (CollectionUtils.isNotEmpty(dqcJobIds)) {
			jobOnlineService.deleteByJobIds(dqcJobIds);
		}

		// 线下操作
		jobDependsService.remove(jobIds);
		jobService.editOff(jobIds, userId, isAdmin);
		alarmService.del(jobIds, userId, isAdmin);

		// 实例删除
		List<Long> taskIds = taskService.listIdsByJobId(jobIds);
		if (CollectionUtils.isNotEmpty(taskIds)) {
			taskDependsService.removeByIds("task_id", taskIds);
		}
		taskService.delByJobIds(jobIds, null);

		// 删除血缘关系
		tableLineageBizService.deleteOnlineLineage(jobIds);
		return true;
	}

	private void checkPermission(List<Long> jobIds,LoginUserDto userDto) {
		Long userId = userDto.getLocalUserId();
		Boolean isAdmin = userDto.getIsAdmin();
		if (BooleanUtils.isTrue(isAdmin)) {
			return;
		}
		List<Job> jobs = jobService.listByIds(jobIds);
		for (Job job : jobs) {
			if (!userId.equals(job.getCreateUser())) {
				throw new BizException(ErrorCode.JOB_NOT_PERMISSION.setParams(job.getName()));
			}
		}
	}

	@Transactional
	public Boolean enable(List<Long> ids, LoginUserDto userDto) {
		checkPermission(ids, userDto);
		
		Job job = new Job();
		job.setStatus(JobStatusEnum.OFF);
		job.setIsDeleted(false);
		job.setIds(ids);
		if (BooleanUtils.isFalse(userDto.getIsAdmin())) {
			job.setCreateUser(userDto.getLocalUserId());
		}
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("check_user", 0L);
		updateParam.put("update_user", userDto.getLocalUserId());
		updateParam.put("status", JobStatusEnum.DOING);
		job.setUpdateParam(updateParam);
		jobService.edit(job);
		return true;
	}

	public JobEnumVo getJobEnum(JobPageType type) {

		JobEnumVo enumVo = new JobEnumVo();
		setJobType(type, enumVo);
		
		enumVo.setRunCounts(generateRunCount(type));

		enumVo.setPriority(Lists.newArrayList(PriorityEnum.values()).stream().map(runEnum -> {
			Content content = new Content();
			content.setValue(runEnum.getCode());
			content.setDesc(runEnum.getDesc());
			return content;
		}).collect(Collectors.toList()));
		enumVo.setFormats(listEnums(FormatEnum.values()));
		enumVo.setCycleTypes(listEnums(JobCycleTypeEnum.values()));
		enumVo.setTaskStatus(listEnums(TaskStatusEnum.values()));
		enumVo.setAlarms(listEnums(AlarmTypeEnum.values()));
		genAlarmNoticeType(enumVo);
		enumVo.setIncrementTypes(listEnums(IncrementTypeEnum.values()));
		enumVo.setMachines(ListMachines());
		enumVo.setJobStatus(listEnums(JobStatusEnum.values()));
		enumVo.setProgramTypes(listEnums(ProgramTypeEnum.values()));
		enumVo.setDeployModes(listEnums(DeployModeEnum.values()));

		// 分task
		enumVo.setDbSources(Lists.newArrayList());
		enumVo.setDbs(Lists.newArrayList());
		if (type != JobPageType.TASK) {
			List<Content> dbSources = this.listDbs(type);
			enumVo.setDbSources(dbSources);
			List<Content> dbs = dbSources.stream().map(vo -> {
				Content content = new Content();
				content.setDesc(vo.getDesc());
				content.setName(vo.getName());
				content.setValue(vo.getValue());
				return content;
			}).collect(Collectors.toList());
			enumVo.setDbs(dbs);
		}

		return enumVo;
	}

	private void genAlarmNoticeType(JobEnumVo enumVo) {
		List<AlarmNoticeTypeEnum> alarmNoticeTypeLists = Lists.newArrayList();
		alarmNoticeTypeLists.add(AlarmNoticeTypeEnum.EMAIL);
		alarmNoticeTypeLists.add(AlarmNoticeTypeEnum.IM);
		alarmNoticeTypeLists.add(AlarmNoticeTypeEnum.PHONE_NO);
		List<Content> contents = alarmNoticeTypeLists.stream().map(alarm -> {
			Content content = new Content();
			content.setValue(alarm.name());
			content.setDesc(alarm.getDesc());
			return content;
		}).collect(Collectors.toList());
		enumVo.setAlarmTypes(contents);
	}

	private List<Content> generateRunCount(JobPageType type) {
		List<RunCountEnum> RunCountEnums = Lists.newArrayList();
		if (JobPageType.DBETL == type) {
			RunCountEnums.add(RunCountEnum.SIMPLE);
			RunCountEnums.add(RunCountEnum.MILLION);
			RunCountEnums.add(RunCountEnum.TEN_MILLION);
		} else if (JobPageType.DBSYNC == type) {
			RunCountEnums.add(RunCountEnum.SIMPLE);
			RunCountEnums.add(RunCountEnum.REFULE_MILLION);
			RunCountEnums.add(RunCountEnum.REFULE_TEN_MILLION);
		}
		return RunCountEnums.stream().map(runEnum -> {
			Content content = new Content();
			content.setValue(runEnum.getCode());
			content.setDesc(runEnum.getDesc());
			return content;
		}).collect(Collectors.toList());
	}

	private void setJobType(JobPageType type, JobEnumVo enumVo) {
		List<Content> contents = Lists.newArrayList();
		List<JobType> allTypes = JobTypeCache.allType();
		// 插入类型
		if (JobPageType.DBETL == type) {
			allTypes.stream().filter(JobType::isDataCrawler).forEach(jobType -> {
				contents.add(addTypeContent(jobType));
			});
		} else if (JobPageType.DBSYNC == type) {
			allTypes.stream().filter(JobType::isDataPush).forEach(jobType -> {
				contents.add(addTypeContent(jobType));
			});
		} else if (JobPageType.CAL == type) {
			allTypes.stream().filter(JobType::isCal).forEach(jobType -> {
				contents.add(addTypeContent(jobType));
			});
		} else {
			List<String> distinctTypes = Lists.newArrayList();
			for (JobType jobType : allTypes) {
				if (!distinctTypes.contains(jobType.getPluginName())) {
					contents.add(addTypeContent(jobType));
					distinctTypes.add(jobType.getPluginName());
				}
			}
		}
		// 插入子方法
		if (JobPageType.TASK != type) {
			addChildByJobType(type, contents);
		}
		
		addCycleType(contents);
		
		enumVo.setJobTypes(contents);
	}

	private void addCycleType(List<Content> contents) {
		for (Content content: contents) {
			if (content.isStream()) {
				List<Content> subContents = Lists.newArrayList(JobCycleTypeEnum.values()).stream()
				.filter(cycleType -> cycleType == JobCycleTypeEnum.REAL_TIME).map(typeEnum -> {
					Content subcontent = new Content();
					subcontent.setValue(typeEnum.name());
					subcontent.setDesc(typeEnum.getDesc());
					return subcontent;
				}).collect(Collectors.toList());
				content.setCycleTypes(subContents);
			} else {
				List<Content> subContents = Lists.newArrayList(JobCycleTypeEnum.values()).stream()
						.filter(cycleType -> cycleType != JobCycleTypeEnum.REAL_TIME).map(typeEnum -> {
							Content subcontent = new Content();
							subcontent.setValue(typeEnum.name());
							subcontent.setDesc(typeEnum.getDesc());
							return subcontent;
						}).collect(Collectors.toList());
						content.setCycleTypes(subContents);
			}
		}
	}

	private Content addTypeContent(JobType jobType) {
		Content content = new Content();
		// PYTHON JAVA HIVE
		content.setValue(jobType.getPluginName());
		// PYTHON任务 JAVA任务 HIVE任务
		content.setDesc(jobType.getPluginAlias());
		content.setStream(jobType.getIsStream() == null ? false : jobType.getIsStream());
		return content;
	}

	private void addChildByJobType(JobPageType type, List<Content> contents) {
		// CAC_PYTHON COLLECT_PYTHON
		Map<String, List<MachineVO>> machineMap = machineService.getSlavesMapByType();
		for (Content content : contents) {
			String key = type.getType() + "_" + content.getValue();
			// key格式形如CAC_PYTHON COLLECT_PYTHON
			List<MachineVO> machines = machineMap.get(key);
			List<Content> subContents = addMachineContent(machines);
			content.setChilds(subContents);
		}
	}

	private List<Content> listDbs(JobPageType jobPageType) {
		List<CollectDbVO> allCollectDbVO = Lists.newArrayList();
		if (jobPageType.getDbFeatureEnum() != null) {
			List<CollectDbVO> collectDbVOs = collectDbService.listDataSource(jobPageType.getDbFeatureEnum());
			allCollectDbVO.addAll(collectDbVOs);
		}

		List<CollectDbVO> commonCollectDbVOs = collectDbService.listDataSource(DbFeatureEnum.COMMON);
		allCollectDbVO.addAll(commonCollectDbVOs);

		if (CollectionUtils.isEmpty(allCollectDbVO)) {
			return Lists.newArrayList();
		}
		return allCollectDbVO.stream().map(db -> {
			Content content = new Content();
			content.setValue(db.getId());
			content.setDesc(db.getDsName());
			content.setName(db.getDbName());
			return content;
		}).sorted(Comparator.comparing(Content::getDesc)).collect(Collectors.toList());
	}

	private List<Content> ListMachines() {
		List<MachineVO> machineVOs = machineService.listSlaves();
		return addMachineContent(machineVOs);
	}

	private List<Content> addMachineContent(List<MachineVO> machineVOs) {
		List<Content> contents = Lists.newArrayList();
		if (CollectionUtils.isNotEmpty(machineVOs)) {
			contents = machineVOs.stream().map(machine -> {
				Content content = new Content();
				content.setValue(machine.getId());
				content.setDesc(machine.getName());
				return content;
			}).collect(Collectors.toList());
		}
		// 系统必填
		Content sysContent = new Content();
		sysContent.setValue(JobConstant.SYSTEM_MACHINE_ID);
		sysContent.setDesc(JobConstant.SYSTEM_MACHINE_NAME);

		contents.add(0, sysContent);
		return contents;
	}



	private List<Content> listEnums(IEnum[] values) {
		return Lists.newArrayList(values).stream().map(typeEnum -> {
			Content content = new Content();
			content.setValue(typeEnum.name());
			content.setDesc(typeEnum.getDesc());
			return content;
		}).collect(Collectors.toList());
	}

	@Transactional
	public Boolean copyJob(List<Long> ids, LoginUserDto userDto) {
		Assert.collectionNotEmpty(ids, "任务列表");
		List<Job> jobs = jobService.listByIdsIfNotDelete(ids);
		if (CollectionUtils.isEmpty(jobs)) {
			return false;
		}
		
		for (Job job : jobs) {
			if (JobCategoryEnum.COLLECT == job.getCategroy()) {
				throw new BizException(ErrorCode.JOB_COLLECT_NOT_COPY.setParams(job.getId().toString()));
			}
		}

		Long localUserId = userDto.getLocalUserId();
		String name = "";
		for (Job job : jobs) {
			Long jobId = job.getId();
			name = job.getName()+"_复制";
			job.setName(name);
			job.setCreateUser(localUserId);
			job.setUpdateUser(localUserId);
			job.setStatus(JobStatusEnum.DOING);
			jobService.save(job);

			Long copyJobId = job.getId();
			JobCategoryEnum categroy = job.getCategroy();
			if (JobCategoryEnum.COLLECT == categroy) {
				JobCollect jobCollect = jobCollectService.getByJobId(jobId);
				jobCollect.setJobId(copyJobId);
				jobCollect.setCreateUser(localUserId);
				jobCollect.setUpdateUser(localUserId);
				jobCollectService.save(jobCollect);
			} else if (JobCategoryEnum.CAL == categroy) {
				JobCal jobCal = jobCalService.getByJobId(jobId);
				jobCal.setJobId(copyJobId);
				jobCal.setCreateUser(localUserId);
				jobCal.setUpdateUser(localUserId);
				jobCalService.save(jobCal);
			} else if (JobCategoryEnum.REFLUE == categroy) {
				JobReflue jobRefule = jobReflueService.getByJobId(jobId);
				jobRefule.setJobId(copyJobId);
				jobRefule.setCreateUser(localUserId);
				jobRefule.setUpdateUser(localUserId);
				jobReflueService.save(jobRefule);
			}

			// 复制报警

			alarmService.copy(jobId, copyJobId, localUserId);
		}
		return true;

	}

	public List<VertexBase> listJobByKey(String key) {
		List<JobOnlineVO> jobVOs = jobOnlineService.listByKey(key);
		if (CollectionUtils.isEmpty(jobVOs)) {
			return Lists.newArrayList();
		}

		return jobVOs.stream().filter(vo -> vo.getCycleType() != JobCycleTypeEnum.REAL_TIME)
				.map(vo -> {
			VertexBase vertexBase = new VertexBase();
			vertexBase.setName(vo.getName());
			vertexBase.setId(vo.getJobId());
			vertexBase.setTargetTable(vo.getTargetTable());
			return vertexBase;
		}).collect(Collectors.toList());
	}

	/**
	 * 获取id跟目标表的关系
	 *
	 * @param jobVOs
	 * @return
	 */
	public Map<Long, JobExtCommonDto> getExtJobMap(List<Job> jobVOs) {
		if (CollectionUtils.isEmpty(jobVOs)) {
			return Maps.newHashMap();
		}
		Map<JobCategoryEnum, List<Job>> mapJob = jobVOs.stream().collect(Collectors.groupingBy(Job::getCategroy));
		Map<Long, JobExtCommonDto> commonMap = Maps.newHashMap();
		for (Entry<JobCategoryEnum, List<Job>> entry : mapJob.entrySet()) {
			List<Long> ids = entry.getValue().stream().map(Job::getId).collect(Collectors.toList());
			if (JobCategoryEnum.CAL == entry.getKey()) {
				Map<Long, List<JobCal>> mapData = jobCalService.getMapByJobIds(ids);
				for (Entry<Long, List<JobCal>> subEntry : mapData.entrySet()) {
					JobExtCommonDto commonDto = new JobExtCommonDto();
					JobCal job = subEntry.getValue().get(0);
					BeanUtil.copyBeanNotNull2Bean(job, commonDto);
					commonDto.setJobId(subEntry.getKey());
					commonMap.put(subEntry.getKey(), commonDto);
				}
			} else if (JobCategoryEnum.COLLECT == entry.getKey()) {
				Map<Long, List<JobCollect>> mapData = jobCollectService.getMapByJobIds(ids);
				for (Entry<Long, List<JobCollect>> subEntry : mapData.entrySet()) {
					JobExtCommonDto commonDto = new JobExtCommonDto();
					JobCollect job = subEntry.getValue().get(0);
					BeanUtil.copyBeanNotNull2Bean(job, commonDto);
					commonDto.setJobId(subEntry.getKey());
					commonMap.put(subEntry.getKey(), commonDto);
				}
			} else if (JobCategoryEnum.REFLUE == entry.getKey()) {
				Map<Long, List<JobReflue>> mapData = jobReflueService.getMapByJobIds(ids);
				for (Entry<Long, List<JobReflue>> subEntry : mapData.entrySet()) {
					JobExtCommonDto commonDto = new JobExtCommonDto();
					JobReflue job = subEntry.getValue().get(0);
					BeanUtil.copyBeanNotNull2Bean(job, commonDto);
					commonDto.setJobId(subEntry.getKey());
					commonMap.put(subEntry.getKey(), commonDto);
				}
			}
		}
		return commonMap;
	}

	@Transactional
	public List<Long> addTask(AddTaskDto addTask, LoginUserDto userDto) {
		Assert.notNull(addTask);
		Long jobId = addTask.getJobId();
		Assert.notNull(jobId,"任务id");
		// 线上补录
		String startTaskTime = addTask.getStartTaskTime();
		String endTaskTime = addTask.getEndTaskTime();

		List<Long> taskIds = Lists.newArrayList();
		if (StringUtils.isNotBlank(startTaskTime) && StringUtils.isNotBlank(endTaskTime) ) {
			checkEndDate(endTaskTime);

			taskIds = addOnlineTask(userDto, jobId, startTaskTime, endTaskTime);
		// 线下测试
		} else if (StringUtils.isNotBlank(addTask.getTaskTime())) {
			checkEndDate(addTask.getTaskTime());

			Long id = addOffTask(addTask.getTaskTime(), userDto, jobId);
			if (id != null) {
				taskIds.add(id);
			}
		}

		return taskIds;

	}

	public Long addOffTask(String time, LoginUserDto userDto, Long jobId) {
		Job job = jobService.getById(jobId);
		if (job == null) {
			return null;
		}
		
		if (job.getCycleType() == JobCycleTypeEnum.REAL_TIME) {
			checkRealTimeTask(jobId, true);
		} 
		Map<String, List<String>>  machineMap = machineService.getIpsMap(job.getMachineId());

		// 根据Job类型获取对应集群
		Long clusterId = null;
		Boolean isClusterJob = jobTypeService.isClusterJob(job.getType(), job.getCategroy());
		if (BooleanUtils.isTrue(isClusterJob)) {
			clusterId = clusterService.getClusterIdByType(ClusterTypeEnum.TEST, job.getType());
			if (clusterId == null) {
				throw new BizException(ErrorCode.CLUSTER_NOT_EXIST_ERROR.setParams(job.getId().toString()));
			}
		}
		
		Date date = DateUtil.parse(time, DateFormatPattern.YYYY_MM_DD_HH_MM_SS);
		Task task = genTask(date, job, machineMap, clusterId, userDto.getLocalUserId());
		task.setIsTemp(true);
		task = taskService.save(task);
		return task.getId();
	}

	@Transactional
	protected List<Long> addOnlineTask(LoginUserDto userDto, Long jobId, String startTaskTime, String endTaskTime) {
		JobOnline jobOnline = jobOnlineService.getById(jobId);
		if (jobOnline == null) {
			return Lists.newArrayList();
		}
		
		List<Task> tasks = genTasks(userDto, jobOnline, startTaskTime, endTaskTime);
		if (CollectionUtils.isEmpty(tasks)) {
			return Lists.newArrayList();
		}
		
		if (jobOnline.getCycleType() == JobCycleTypeEnum.REAL_TIME) {
			checkRealTimeTask(jobId, false);
		} else {
			checkExistTask(jobOnline, startTaskTime, endTaskTime);
		}
		
		// 保存
		List<Task> newTasks = Lists.newArrayList();
		if (tasks.size() > TASK_LIMIT) {
			newTasks = tasks.subList(tasks.size() - TASK_LIMIT, tasks.size());
		} else {
			newTasks = tasks;
		}

		if (CollectionUtils.isNotEmpty(newTasks)) {
			taskService.saveBatch(newTasks);
		}

		
		// 保存依赖关系
		taskDependsBizService.saveDepends(newTasks, jobOnline);

		return newTasks.stream().map(Task::getId).collect(Collectors.toList());
	}

	private void checkRealTimeTask(Long jobId, Boolean isTemp) {
		List<TaskVO> taskVos = taskService.listByJobId(jobId, isTemp);
		if (CollectionUtils.isNotEmpty(taskVos)) {
			throw new BizException(ErrorCode.TASK_INSTANCE_EXISTS.setParams(taskVos.get(0).getId().toString()));
		}
	}

	private List<Task> genTasks(LoginUserDto userDto, JobOnline jobOnline, String startTaskTime, String endTaskTime) {
		Map<String, List<String>>  machineMap = machineService.getIpsMap(jobOnline.getMachineId());
		List<TaskTimeDto> timeDtos = jobOnline.getCycleType().listTaskTimes(JSON.parseObject(jobOnline.getSchedulerTime(), SchedulerTimeDto.class)
				,DateUtil.parse(startTaskTime, DateFormatPattern.YYYY_MM_DD_HH_MM_SS)
				,DateUtil.parse(endTaskTime, DateFormatPattern.YYYY_MM_DD_HH_MM_SS));
		List<Task> tasks = Lists.newArrayList();
		for (TaskTimeDto timeDto : timeDtos) {
			Job job = new Job();
			BeanUtil.copyBeanNotNull2Bean(jobOnline, job);
			job.setId(jobOnline.getJobId());

			Task task = genTask(timeDto.getTaskTime(), job, machineMap, job.getClusterId(), userDto.getLocalUserId());
			task.setIsTemp(false);
			tasks.add(task);
		}
		return tasks;
	}

	private void checkExistTask(JobOnline jobOnline, String startTaskTime, String endTaskTime) {
		
		List<TaskTimeDto> timeDtos = jobOnline.getCycleType().listTaskTimes(JSON.parseObject(jobOnline.getSchedulerTime(), SchedulerTimeDto.class)
				,DateUtil.parse(startTaskTime, DateFormatPattern.YYYY_MM_DD_HH_MM_SS)
				,DateUtil.parse(endTaskTime, DateFormatPattern.YYYY_MM_DD_HH_MM_SS));
		if (CollectionUtils.isEmpty(timeDtos)) {
			return;
		}
		
		String startTime = DateUtil.format(timeDtos.get(0).getTaskTime(), DateFormatPattern.YYYY_MM_DD_HH_MM_SS);
		String endTime = DateUtil.format(timeDtos.get(timeDtos.size()-1).getTaskTime(), DateFormatPattern.YYYY_MM_DD_HH_MM_SS);
		List<Task> taskVOs = taskService.listOnlineTaskByDate(startTime, endTime, jobOnline.getJobId());
		if (CollectionUtils.isEmpty(taskVOs)) {
			return;
		}
		for (TaskTimeDto task : timeDtos) {
			for (Task taskVo : taskVOs) {
				if (task.getTaskTime().equals(taskVo.getTaskTime())) {
					throw new BizException(ErrorCode.TASK_INSTANCE_EXISTS.setParams(taskVo.getId().toString()));
				}
			}
		}
	}

	private void checkEndDate(String taskTime) {
		Date endDate = DateUtil.parse(taskTime, DateFormatPattern.YYYY_MM_DD_HH_MM_SS);
		Date date = DateUtil.getMaxOfDay(new Date());
		if (endDate.after(date)) {
			throw new BizException(ErrorCode.JOB_ADD_TASK_AFTER_NOW);
		}
	}

	private Task genTask(Date taskTime, Job job, Map<String, List<String>> machineMap, Long clusterId, Long userId) {
		Task task = new Task();
		task.setJobId(job.getId());
		task.setName(job.getName());
		task.setClusterId(clusterId);
		task.setCategory(job.getCategroy());
		task.setPriority(PriorityEnum.HIGH.getCode());
		task.setStatus(TaskStatusEnum.INIT);
		task.setMachineId(job.getMachineId());
		task.setDepartName(job.getDepartName());
		task.setType(job.getType());
		task.setCreateUser(userId);
		task.setUpdateUser(userId);
		task.setScore(0);
		task.setNoticeCount(0);
		task.setJobType(job.getJobType());
		task.setRetryCount(0);
		task.setRetryDur(job.getRetryDur());
		task.setIsNotice(false);
		task.setCycleType(job.getCycleType());
		task.setRetryMax(job.getRetryMax());
		task.setScriptId(job.getScriptId());
		task.setTaskTime(taskTime);
		task.setStartTime(new Date());
		String key = job.getCategroy().name() + "_" + job.getType();
		// 选择对应功能的调度机
		task.setIp(StringUtil.getRand(machineMap.get(key)));
		return task;
	}

	public List<String> listParamNames(Param param) {
		List<String> keys = paramService.listNames(param.getJobType(), param.getProgramType(), param.getName());
		List<String> sysKeys = paramService.listSysNames(param.getName());
		Optional.ofNullable(keys).orElse(Lists.newArrayList()).addAll(sysKeys);
		return keys;
	}
	
	public void changeJobOwner(ChangeDto changeDto, LoginUserDto userDto) {
		List<Long> jobIds = changeDto.getJobIds();

		Long targetDefaultUserGroupId = userInfoService.getDefaultGroupIdByUserId(changeDto.getTargetOwnerId());
		alarmService.checkAlarmValidByJobIds(jobIds, targetDefaultUserGroupId);

		List<Job> jobs = jobService.listByIds(jobIds);
		if (BooleanUtils.isTrue(userDto.getIsAdmin())) {
			Map<Long, List<Job>> jobMap = jobs.stream().collect(Collectors.groupingBy(Job::getCreateUser));
			for (Entry<Long, List<Job>> entry : jobMap.entrySet()) {
				ChangeDto subChangeDto = new ChangeDto();
				subChangeDto.setIsJob(true);
				subChangeDto.setTargetOwnerId(changeDto.getTargetOwnerId());
				subChangeDto.setOriginOwnerId(entry.getKey());
				List<Long> subJobIds = entry.getValue().stream().map(Job::getId).collect(Collectors.toList());
				subChangeDto.setJobIds(subJobIds);
				changeOwner(subChangeDto, userDto);
			}
		} else {
			for (Job job : jobs) {
				if (!job.getCreateUser().equals(userDto.getLocalUserId())) {
					throw new BizException(ErrorCode.JOB_NOT_PERMISSION.setParams(job.getName()));
				}
			}
			changeDto.setOriginOwnerId(userDto.getLocalUserId());
			changeDto.setIsJob(true);
			changeOwner(changeDto, userDto);
		}
	}
	
	/**
	 * 更改owner
	 * 
	 * @param changeDto
	 */
	@Transactional
	public void changeOwner(ChangeDto changeDto, LoginUserDto userDto) {
		Assert.notNull(changeDto);
		Long originOwnerId = changeDto.getOriginOwnerId();
		Assert.notNull(originOwnerId);
		Long targetOwnerId = changeDto.getTargetOwnerId();
		Assert.notNull(targetOwnerId);
		
		// 修改任务管理
		OwnerOperation jobOperation = (List<Long> reqJobIds, Long userId, Long targetUserId) -> {
			List<JobVO> listByCreateUser = jobService.listByCreateUser(userId);
			List<Long> jobIds = listByCreateUser.stream().map(JobVO::getId).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(jobIds)) {
				return;
			}
			jobIds = listIds(reqJobIds, jobIds, ErrorCode.JOB_NOT_PERMISSION);
			jobService.editOwnerByJobIds(userId, targetUserId, jobIds);
			jobCalService.editOwnerByJobIds(userId, targetUserId, jobIds);
			jobCollectService.editOwnerByJobIds(userId, targetUserId, jobIds);
			jobReflueService.editOwnerByJobIds(userId, targetUserId, jobIds);
			jobOnlineService.editOwnerByJobIds(userId, targetUserId, jobIds);
			alarmService.editOwnerByJobIds(userId, targetUserId, jobIds);
			taskService.editInitOwnerByJobIds(userId, targetUserId, jobIds);
		};

		operate(changeDto.getIsJob(), changeDto.getJobIds(), originOwnerId, targetOwnerId, jobOperation);

		// 修改脚本列表
		OwnerOperation scriptOperation = (List<Long> reqScriptIds, Long userId, Long targetUserId) -> {

			List<Long> scriptIds = scriptService.listByCreateUser(userId).stream().map(ScriptVO::getId)
					.collect(Collectors.toList());
			if (CollectionUtils.isEmpty(scriptIds)) {
				return;
			}
			scriptIds = listIds(reqScriptIds, scriptIds, ErrorCode.SCRIPT_NOT_PERMISSION);
			scriptService.editOwnerByIds(userId, targetUserId, scriptIds);
		};

		operate(changeDto.getIsScript(), changeDto.getScriptIds(), originOwnerId, targetOwnerId, scriptOperation);
		
		// 修改项目列表
		OwnerOperation projectOperation = (List<Long> reqProjectIds, Long userId, Long targetUserId) -> {

			List<Long> projectIds = projectService.listByCreateUser(userId).stream().map(Project::getId)
					.collect(Collectors.toList());
			if (CollectionUtils.isEmpty(projectIds)) {
				return;
			}
			projectIds = listIds(reqProjectIds, projectIds, ErrorCode.PROJECT_NOT_PERMISSION);
			projectService.editOwnerByIds(userId, targetUserId, projectIds);
		};
		operate(changeDto.getIsProject(), changeDto.getProjectIds(), originOwnerId, targetOwnerId, projectOperation);

		// 修改报警
		OwnerOperation alarmOperation =  (List<Long> reqJobIds, Long userId, Long targetUserId) -> {
			Long targetDefaultUserGroupId = userInfoService.getDefaultGroupIdByUserId(targetUserId);
			alarmService.editUserGroupIdByJobIds(reqJobIds, targetDefaultUserGroupId);
		};
		operate(changeDto.getIsJob(), changeDto.getJobIds(), originOwnerId, targetOwnerId, alarmOperation);
	}

	private List<Long> listIds(List<Long> reqIds, List<Long> ids, ErrorCode errorCode) {
		Assert.collectionNotEmpty(reqIds, "请求数据");
		Assert.collectionNotEmpty(ids, "数据库数据");
		
		for (Long reqId : reqIds) {
			if (!ids.contains(reqId)) {
				throw new BizException(errorCode.setParams(reqId.toString()));
			}
		}
		return reqIds;
	}
	
	private void operate(Boolean isTrue, List<Long> ids, Long originOwnerId, Long targetOwnerId, OwnerOperation ownerOperation) {
		if (BooleanUtils.isTrue(isTrue)) {
			ownerOperation.change(ids, originOwnerId, targetOwnerId);
		}
			
	}
	
	interface OwnerOperation {
		void change( List<Long> ids, Long originOwnerId, Long targetOwnerId);
	}

}
