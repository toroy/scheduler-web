package com.bigdata.platform.scheduler.web.server.service;

import com.alibaba.fastjson.JSON;
import com.bigdata.platform.scheduler.web.core.service.*;
import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.common.exception.BizException;
import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.common.util.BeanUtil;
import com.bigdata.platform.scheduler.common.util.DateUtil;
import com.clubfactory.platform.meta.client.dto.TableSimpleDto;
import com.clubfactory.platform.meta.client.enums.DbType;
import com.clubfactory.platform.meta.client.utils.DbUtil;
import com.bigdata.platform.scheduler.dal.dto.SchedulerTimeDto;
import com.bigdata.platform.scheduler.dal.dto.SubscribeDto;
import com.bigdata.platform.scheduler.dal.dto.TaskTimeDto;
import com.bigdata.platform.scheduler.dal.enums.*;
import com.bigdata.platform.scheduler.dal.po.CollectDb;
import com.bigdata.platform.scheduler.dal.po.Job;
import com.bigdata.platform.scheduler.dal.po.JobOnline;
import com.bigdata.platform.scheduler.dal.po.Task;
import com.bigdata.platform.scheduler.web.core.dto.JobExtCommonDto;
import com.bigdata.platform.scheduler.web.core.enums.ErrorCode;
import com.bigdata.platform.scheduler.web.core.utils.SpringBean;
import com.bigdata.platform.scheduler.web.core.utils.StreamApiUtils;
import com.bigdata.platform.scheduler.web.core.vo.CollectDbVO;
import com.bigdata.platform.scheduler.web.core.vo.TableOnlineLineageVO;
import com.bigdata.platform.scheduler.web.server.constant.JobConstant;
import com.bigdata.platform.scheduler.web.server.dqc.service.DqcRuleBizService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JobCheckBizService {
	
	@Resource
	JobService jobService;
	@Resource
	TaskService taskService;
	@Resource
	JobOnlineService jobOnlineService;
	@Resource
	NoticeBizService noticeBizService;
	@Resource
    ScriptService scriptService;
	@Resource
    UserService userService;
	@Resource
    TableOnlineLineageService tableOnlineLineageService;
	@Resource
	ClusterService clusterService;
	@Resource
	JobBizService jobBizService;
	@Resource
	JobTypeService jobTypeService;
	@Resource
	SubscribeBizService subscribeBizService;
	@Resource
    CollectDbService collectDbService;
	@Resource
	JobCollectService jobCollectService;
	@Resource
    JobReflueService jobReflueService;
	@Resource
	DqcRuleBizService dqcRuleBizService;
	@Resource
	JobDetailBizService jobDetailBizService;
	@Resource
	TableLineageBizService tableLineageBizService;

    ExecutorService executor = new ThreadPoolExecutor(200, 200,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(2000));
	

	public void checkJobs(List<Long> jobIds, LoginUserDto userDto) {
		Assert.collectionNotEmpty(jobIds, "任务id");
		
		Map<Long, Job> jobMap = jobService.getMapByIds(jobIds);
		if (MapUtils.isEmpty(jobMap)
				|| jobIds.size() != jobMap.size()) {
			throw new BizException(ErrorCode.JOB_NOT_EXISTE);
		}
		jobService.editStatus(jobIds, JobStatusEnum.CHECK, userDto.getLocalUserId());
		
		List<Task> tasks = taskService.listByJobIds(jobIds, true);
		Map<Long, List<Task>> taskMap = tasks.stream().collect(Collectors.groupingBy(Task::getJobId));
		Map<Long, JobOnline> jobOnlineMap = jobOnlineService.getMapByIds(jobIds);

        for (Long jobId : jobIds) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    noticeBizService.noticeCheck(taskMap.get(jobId), jobMap.get(jobId), jobOnlineMap.get(jobId), userDto);
                }
            });
        }
        // 自动审核
        List<Long> autoJobIds = autoCheckJobs(jobIds, jobMap, jobOnlineMap, taskMap);
        if (CollectionUtils.isNotEmpty(autoJobIds)) {
            LoginUserDto autoUserDto = new LoginUserDto();
            autoUserDto.setLocalUserId(-1L);
            autoUserDto.setIsAdmin(true);

            executor.submit(new Runnable() {
                JobCheckBizService jobCheckBizService = SpringBean.getBean(JobCheckBizService.class);
                @Override
                public void run() {
                    jobCheckBizService.editSuccessByJobIds(autoJobIds, autoUserDto);
                }
            });
        }
    }


	private void checkJobName(List<Job> reqJobs) {
		List<String> names = reqJobs.stream().map(Job::getName).collect(Collectors.toList());
		Map<String, List<JobOnline>> jobOnlineMap = jobOnlineService.getMapByNames(names);
		for (Job job : reqJobs) {
			List<JobOnline> jobOnlines = jobOnlineMap.get(job.getName());
			if (CollectionUtils.isEmpty(jobOnlines)) {
				return;
			}
			if (jobOnlines.size() > 1) {
				throw new BizException(ErrorCode.JOB_ONLINE_NAME_REPEAT_ERROR.setParams(job.getName()));
			}
			if (!jobOnlines.get(0).getJobId().equals(job.getId())) {
				throw new BizException(ErrorCode.JOB_ONLINE_NAME_REPEAT_ERROR.setParams(job.getName()));
			}
		}
	}
	
	public Boolean editSuccessByJobId(Long jobId, LoginUserDto userDto) {
		Job job = new Job();
		job.setId(jobId);
		job.setStatus(JobStatusEnum.DOING);
        return this.editSuccess(Lists.newArrayList(job), userDto);
	}

	private void editTask(List<Job> jobs) {
		List<Long> jobIds = jobs.stream().map(Job::getId).collect(Collectors.toList());
		Map<Long, JobOnline> jobOnlineMap = jobOnlineService.getMapByIds(jobIds);
		for (Job job : jobs) {
			Long jobId = job.getId();
			JobOnline jobOnline = jobOnlineMap.get(jobId);
			if (jobOnline == null) {
				continue;
			}
			Task task = new Task();
			// 改脚本
			if (!jobOnline.getScriptId().equals(job.getScriptId())) {
				task.setScriptId(job.getScriptId());
			}
			// 改任务类型
			if (jobOnline.getType() != job.getType()) {
				task.setType(job.getType());
			}
			// 改调度机
			if (!jobOnline.getMachineId().equals(job.getMachineId())) {
				task.setMachineId(job.getMachineId());
			}
			// 改优先级
			if (!jobOnline.getPriority().equals(job.getPriority())) {
				task.setPriority(job.getPriority());
			}
			
			if (task.getScriptId() != null 
					|| task.getType() != null 
					|| task.getMachineId() != null 
					|| task.getPriority() != null) {
				taskService.editByJobId(jobId, task);
			}
			
			// 改天任务, t+1任务的调度时间
			Date currentDate = new Date();
			if (!StringUtils.equalsIgnoreCase(job.getSchedulerTime(), jobOnline.getSchedulerTime())) {
				if ((JobCycleTypeEnum.DAY == job.getCycleType() && JobCycleTypeEnum.DAY == jobOnline.getCycleType())) {
					SchedulerTimeDto schedulerTimeDto =	JSON.parseObject(job.getSchedulerTime(), SchedulerTimeDto.class);
					List<TaskTimeDto> taskTimeDtos = job.getCycleType().listTaskTimes(schedulerTimeDto, DateUtil.getMinOfDay(currentDate));
					Date newStartdate = taskTimeDtos.get(0).getStartTime();
					if (newStartdate.after(currentDate)) {
						taskService.editStartTimeByJobId(jobId, newStartdate, currentDate);
					}
				}
			}
			
			// 暂停恢复实例
			taskService.editResumeByDate(currentDate, jobId);
		}
	}

	private void setNewVersion(List<Job> jobs) {
		Map<Long, Integer> mapVersionMap = scriptService.getScriptVersionMap();
		for (Job job : jobs) {
			job.setVersion(mapVersionMap.get(job.getScriptId()));
		}
	}

	private List<JobOnline> saveOnlineJob(LoginUserDto userDto, List<Job> jobs, Boolean isRunNow) {
		List<JobOnline> jobOnlines = genJobOnline(userDto, jobs);

		setIsRunning(jobOnlines, isRunNow);

		setClusterId(jobs, jobOnlines);

		jobOnlineService.saveBatch(jobOnlines);

		return jobOnlines;
	}

	@NotNull
	private List<JobOnline> genJobOnline(LoginUserDto userDto, List<Job> jobs) {
		Map<Long, JobExtCommonDto> extJobMap = jobBizService.getExtJobMap(jobs);
		return jobs.stream().map(job ->{
			JobOnline jobOnline = new JobOnline();
			BeanUtil.copyBeanNotNull2Bean(job, jobOnline);
			JobExtCommonDto jobExtCommonDto = Optional.ofNullable(extJobMap.get(job.getId())).orElse(new JobExtCommonDto());
			BeanUtil.copyBeanNotNull2Bean(jobExtCommonDto, jobOnline);
			jobOnline.setJobId(job.getId());
			jobOnline.setCheckUser(userDto.getLocalUserId());
			jobOnline.setStatus(JobStatusEnum.ONLINE);
			return jobOnline;
		}).collect(Collectors.toList());
	}

	private void setClusterId(List<Job> jobs, List<JobOnline> jobOnlines) {
		// 集群分配
		List<Long> createUserIds = jobs.stream().map(Job::getCreateUser).collect(Collectors.toList());
		Map<Long, Integer> departIdMap = userService.getDepartIdMap(createUserIds);
		for (JobOnline jobOnline : jobOnlines) {
			// 判断是否需要集群
			Boolean isClusterJob = jobTypeService.isClusterJob(jobOnline.getType(), jobOnline.getCategroy());
			if (BooleanUtils.isTrue(isClusterJob)) {
				Long clusterId = getClusterId(departIdMap, jobOnline.getType(), jobOnline.getName(), jobOnline.getCreateUser());
				jobOnline.setClusterId(clusterId);
			}
		}
	}

	private void setIsRunning(List<JobOnline> jobOnlines, Boolean isRunNow) {
		// 判断是否立即开跑,如果当天有线上实例存在，则次日跑
		// 月，周，不, 实时自动生成实例
		String startDate = LocalDate.now().toString();
		String endDate = LocalDate.now().plusDays(1).toString();
		for (JobOnline jobOnline : jobOnlines) {
			// 主动设置，不再判断
			if (isRunNow != null) {
				jobOnline.setIsRunning(isRunNow);
				continue;
			}

			jobOnline.setIsRunning(true);
			if (JobCycleTypeEnum.WEEK == jobOnline.getCycleType()
					|| JobCycleTypeEnum.MONTH == jobOnline.getCycleType()
					|| JobCycleTypeEnum.REAL_TIME == jobOnline.getCycleType()) {
				jobOnline.setIsRunning(false);
				continue;
			}

			List<Task> tasks = taskService.listOnlineTaskByDate(startDate, endDate, jobOnline.getJobId());
			if (CollectionUtils.isNotEmpty(tasks)) {
				jobOnline.setIsRunning(false);
			}
		}
	}
	
	private Long getClusterId(Map<Long, Integer> departIdMap, String type, String name, Long createUser) {
		Integer departId = departIdMap.get(createUser);
		Long clusterId = clusterService.getClusterByJob(type, departId);
		if (clusterId == null) {
			clusterId = clusterService.getClusterIdByType(ClusterTypeEnum.COMMON, type);
		}
		if (clusterId == null) {
			throw new BizException(ErrorCode.CLUSTER_NOT_EXIST_ERROR.setParams(name));
		}
		return clusterId;
	}

	public void editSuccessByJobIds(List<Long> reqJobIds, LoginUserDto userDto) {
		editSuccessByJobIds(reqJobIds, null, userDto);
	}

	@Transactional(rollbackFor = Exception.class, timeout = 20)
	public void editSuccessByJobIds(List<Long> reqJobIds, Boolean isRunNow, LoginUserDto userDto) {
		if (BooleanUtils.isFalse(userDto.getIsAdmin())) {
			throw new BizException(ErrorCode.NOT_ADMIN_ERROR);
		}

		// 查询审核的状态
		List<Job> jobs = jobService.listNeedPassByIds(reqJobIds);
		if (CollectionUtils.isEmpty(jobs)) {
			throw new BizException(ErrorCode.JOB_NOT_CHECK);
		}
		
		// 查询表名是否跟线上冲突
		checkJobName(jobs);
		
		// 更新实例的信息
		editTask(jobs);
		
		List<Long> ids = jobs.stream().map(Job::getId).collect(Collectors.toList());
		// 置为成功
		jobService.editSuccessByStatus(ids, null, userDto.getLocalUserId());
		
		// job线上的逻辑删除
		jobOnlineService.deleteByJobIds(ids);
		
		// 获取脚本新版本
		setNewVersion(jobs);
		//保存job线上信息
		List<JobOnline> jobOnlines = saveOnlineJob(userDto, jobs, isRunNow);

		executor.submit(new Runnable() {
			@Override
			public void run() {
				// 解析血缘关系
				tableLineageBizService.parseLineage(jobOnlines);
				// 通知
				noticeBizService.checkBatchSuccess(userDto, jobs);
				// 生成依赖订阅信息
				JobCheckBizService.this.subscribeDepTables(ids, jobs);
			}
		});

		// DQC任务重新上线
		executor.submit(new Runnable() {
			@Override
			public void run() {
				for (Job job : jobs) {
					dqcRuleBizService.updateJobScheduler(job.getId(), Lists.newArrayList(), job.getSchedulerTimeDto(), job.getCycleType(), userDto);
				}
			}
		});
	}


	/**
	 * 订阅父依赖表
	 * @param jobIdList
	 * @param jobs
	 */
	private void subscribeDepTables(List<Long> jobIdList, List<Job> jobs) {
		// 根据血缘关系生成依赖订阅信息
		List<TableSimpleDto> lineageTableSimpleDtoList = this.genLineageSubscribeItems(jobIdList);
		// ETL Job根据配置生成依赖订阅信息
		if (Objects.isNull(lineageTableSimpleDtoList)) {
			lineageTableSimpleDtoList = Lists.newArrayList();
		}
		List<TableSimpleDto> etlTableSimpleDtoList = this.genEtlJobSubscribeItems(jobs);
		if (CollectionUtils.isNotEmpty(etlTableSimpleDtoList)) {
			lineageTableSimpleDtoList.addAll(etlTableSimpleDtoList);
		}

		List<TableSimpleDto> tableSimpleDtoList = lineageTableSimpleDtoList.stream()
				.filter(item -> StringUtils.isNotBlank(item.getDbName()) && StringUtils.isNotBlank(item.getDbSource())
						&& StringUtils.isNotBlank(item.getName()))
				.filter(StreamApiUtils.distinctByKey(item -> String.format("%s_%s_%s_%s", item.getDbSource().trim(),
						item.getDbName().trim(), item.getName().trim(), item.getJobCreateUser())))
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(tableSimpleDtoList)) {
			return;
		}
		this.saveDepSubscribeItems(tableSimpleDtoList);
	}

	/**
	 * 为当前用户生成jobIdList中对应表的依赖订阅信息
	 * @param jobIdList
	 */
	private List<TableSimpleDto> genLineageSubscribeItems(List<Long> jobIdList) {
		if (CollectionUtils.isEmpty(jobIdList)) {
			return Lists.newArrayList();
		}
		// 根据申请审核的JobIdList获取本次审核成功的任务父血缘信息列表
		List<TableOnlineLineageVO> tableOnlineLineageList = tableOnlineLineageService.listByJobIds(jobIdList, LineageTypeEnum.PARENT);
		if (CollectionUtils.isEmpty(tableOnlineLineageList)) {
			return Lists.newArrayList();
		}
		List<Long> tableOnlineIdList = tableOnlineLineageList.stream()
				.filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getId()))
				.map(TableOnlineLineageVO::getId)
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(tableOnlineIdList)) {
			return Lists.newArrayList();
		}

		List<SubscribeDto> subscribeDtoList = tableOnlineLineageService.listSubscribeInfosByIds(tableOnlineIdList);
		Assert.collectionNonEmpty(subscribeDtoList, "获取任务血缘信息失败");
		return subscribeDtoList.stream()
				.filter(subscribeDto -> Objects.nonNull(subscribeDto) && Objects.nonNull(subscribeDto.getJobCreateUser()))
				.map(subscribeDto -> {
					TableSimpleDto tableSimpleDto = new TableSimpleDto();
					tableSimpleDto.setDbName(subscribeDto.getDbName());
					DbType dbType = com.bigdata.platform.scheduler.dal.enums.DbType.HIVE == subscribeDto.getDbType() ?
							DbType.HIVE : null;
					tableSimpleDto.setDbSource(DbUtil.getDbSource(subscribeDto.getDbHost(), dbType));
					tableSimpleDto.setName(subscribeDto.getTableName());
					tableSimpleDto.setJobCreateUser(subscribeDto.getJobCreateUser());
					return tableSimpleDto;
				}).collect(Collectors.toList());
	}

	/**
	 * 根据采集回流任务中的配置生成依赖订阅信息
	 * @param jobs
	 */
	private List<TableSimpleDto> genEtlJobSubscribeItems(List<Job> jobs) {
		List<TableSimpleDto> tableSimpleDtoList = Lists.newArrayList();
		if (CollectionUtils.isEmpty(jobs)) {
			return tableSimpleDtoList;
		}
		List<Long> collectJobIds = jobs.stream()
				.filter(job -> JobCategoryEnum.COLLECT == job.getCategroy())
				.map(Job::getId)
				.collect(Collectors.toList());
		List<Long> reflueJobIds = jobs.stream()
				.filter(job -> JobCategoryEnum.REFLUE == job.getCategroy())
				.map(Job::getId)
				.collect(Collectors.toList());

		if (CollectionUtils.isEmpty(collectJobIds) && CollectionUtils.isEmpty(reflueJobIds)) {
			return tableSimpleDtoList;
		}

		List<SubscribeDto> etlJobSubscribeInfos = Lists.newArrayList();
		if (CollectionUtils.isNotEmpty(collectJobIds)) {
			etlJobSubscribeInfos.addAll(this.jobCollectService.listSubscribeInfosByJobId(collectJobIds));
		}
		if (CollectionUtils.isNotEmpty(reflueJobIds)) {
			etlJobSubscribeInfos.addAll(this.jobReflueService.listSubscribeInfosByJobId(reflueJobIds));
		}
		if (CollectionUtils.isEmpty(etlJobSubscribeInfos)) {
			return tableSimpleDtoList;
		}

		Map<Long, CollectDbVO> dsIdToDataSource = this.listCollectDbList();
		if (CollectionUtils.isNotEmpty(etlJobSubscribeInfos)) {
			for (SubscribeDto subscribeDto: etlJobSubscribeInfos) {
				if (subscribeDto.getJobCreateUser() == null) {
					continue;
				}
				if (StringUtils.isBlank(subscribeDto.getTableName())) {
					continue;
				}
				CollectDbVO collectDbVO = dsIdToDataSource.get(subscribeDto.getDataSourceId());
				if (collectDbVO == null) {
					continue;
				}
				TableSimpleDto tableSimpleDto = new TableSimpleDto();
				tableSimpleDto.setJobCreateUser(subscribeDto.getJobCreateUser());
				tableSimpleDto.setName(jobDetailBizService.getTableName(subscribeDto.getTableName()));
				tableSimpleDto.setDbName(collectDbVO.getDbName());
				DbType dbType = com.bigdata.platform.scheduler.dal.enums.DbType.HIVE == collectDbVO.getDsType() ?
						DbType.HIVE : null;
				tableSimpleDto.setDbSource(DbUtil.getDbSource(collectDbVO.getDbHost(), dbType));
				tableSimpleDtoList.add(tableSimpleDto);
			}
		}
		if (CollectionUtils.isNotEmpty(tableSimpleDtoList)) {
			tableSimpleDtoList = tableSimpleDtoList.stream()
					.filter(item -> Objects.nonNull(item.getName()))
					.peek(item -> {
						String[] tableNameItems = item.getName().split("\\.");
						if (tableNameItems.length == 2) {
							item.setName(tableNameItems[1]);
							item.setDbName(tableNameItems[0]);
						}
					})
					.collect(Collectors.toList());
		}
		return tableSimpleDtoList;
	}

	/**
	 * 获取dsId -> ColloctDB
	 * @return
	 */
	private Map<Long, CollectDbVO> listCollectDbList() {
		CollectDb collectDbWhere = new CollectDb();
		collectDbWhere.setIsDeleted(false);
		collectDbWhere.setStatus(CommonStatus.ENABLED);
		List<CollectDbVO> collectDbList = this.collectDbService.list(collectDbWhere);
		if (CollectionUtils.isEmpty(collectDbList)) {
			return Maps.newHashMap();
		}
		Map<Long, CollectDbVO> dsIdToDataSource = Maps.newHashMap();
		collectDbList.stream().filter(collectDbVO -> collectDbVO.getId() != null)
				.forEach(collectDbVO -> dsIdToDataSource.put(collectDbVO.getId(), collectDbVO));
		return dsIdToDataSource;
	}

	/**
	 * 保存订阅信息
	 * @param tableSimpleDtoList
	 */
	private void saveDepSubscribeItems(List<TableSimpleDto> tableSimpleDtoList) {
		Assert.collectionNonEmpty(tableSimpleDtoList,  "订阅列表不能为空");
		this.subscribeBizService.depSubscribe(tableSimpleDtoList);
	}

	/**
	 * 1. 暂停->上线：线上job改为上线，对应暂停的task 都改为init。如果线下job状态为暂停改为上线，否则不改
	 * 2. 审核中，下线->上线：线下job改为上线，并删除线上job数据以及job依赖数据。将线下job数据复制到线上job，线下依赖复制到线上依赖job
	 *
	 * @param reqJobs
	 * @param userDto
	 * @return
	 */
	public Boolean editSuccess(List<Job> reqJobs, LoginUserDto userDto) {
		if (CollectionUtils.isEmpty(reqJobs)) {
			return false;
		}
		List<Long> reqJobIds = reqJobs.stream().map(Job::getId).collect(Collectors.toList());
		
		editSuccessByJobIds(reqJobIds, userDto);

		return true;
	}


	private List<Long> autoCheckJobs(List<Long> jobIds, Map<Long, Job> jobMap, Map<Long, JobOnline> jobOnlineMap, Map<Long, List<Task>> tmpTaskMap) {
	    List<Long> autoJobIds = Lists.newArrayList();
		for (Long jobId : jobIds) {
			Job job = jobMap.get(jobId);
            JobOnline jobOnline = jobOnlineMap.get(jobId);
            List<Task> tmpTasks = tmpTaskMap.get(jobId);
            // 第一次审核
            if (jobOnline == null) {
                if (!checkTask(null, tmpTasks)) {
                    continue;
                }
				if (!job.getMachineId().equals(JobConstant.SYSTEM_MACHINE_ID)) {
					continue;
				}
				if (job.getPriority() == PriorityEnum.HIGH.getCode()) {
					continue;
				}
				if (BooleanUtils.isTrue(job.getRunOnTmpEmr())) {
					continue;
				}
				if (JobCycleTypeEnum.MINUTES == job.getCycleType() || JobCycleTypeEnum.HOURS == job.getCycleType() ||  JobCycleTypeEnum.REAL_TIME == job.getCycleType()) {
					continue;
				}
				if (job.getRetryMax() >= JobConstant.CHECK_RETRY_MAX) {
					continue;
				}
                autoJobIds.add(jobId);
				// 已有任务的审核
			} else {
				if (!job.getScriptId().equals(jobOnline.getScriptId())) {
                    if (!checkTask(jobOnline.getCreateTime(), tmpTasks)) {
                        continue;
                    }
                } else {
				    if (!scriptService.getVersion(job.getScriptId()).equals(jobOnline.getVersion())) {
                        if (!checkTask(jobOnline.getCreateTime(), tmpTasks)) {
                            continue;
                        }
                    }
                }

				if (!job.getMachineId().equals(jobOnline.getMachineId())) {
                    if (!job.getMachineId().equals(JobConstant.SYSTEM_MACHINE_ID)) {
                        continue;
                    }
                }

				if (!job.getPriority().equals(jobOnline.getPriority())) {
                    if (job.getPriority() == PriorityEnum.HIGH.getCode()) {
                        continue;
                    }
                }

                if (job.getRunOnTmpEmr() != null &&
                        !job.getRunOnTmpEmr().equals(jobOnline.getRunOnTmpEmr())) {
                    if (BooleanUtils.isTrue(job.getRunOnTmpEmr())) {
                        continue;
                    }
                }

                if (!job.getCycleType().equals(jobOnline.getCycleType())) {
                    if (JobCycleTypeEnum.REAL_TIME == job.getCycleType()) {
                        continue;
                    }
                }

				if (JobCycleTypeEnum.MINUTES == job.getCycleType() || JobCycleTypeEnum.HOURS == job.getCycleType()) {
					continue;
				}

                if (!job.getRetryMax().equals(jobOnline.getRetryMax())) {
                    if (job.getRetryMax() >= JobConstant.CHECK_RETRY_MAX) {
                        continue;
                    }
                }
                autoJobIds.add(jobId);
			}
		}
		
		return autoJobIds;
	}

    private Boolean checkTask(Date updateTime, List<Task> tmpTasks) {
    	if (CollectionUtils.isEmpty(tmpTasks)) {
    		 return false;
    	}
    	
        Task tempTask = tmpTasks.stream().filter(task -> task.getEndTime() != null)
                .filter(task -> TaskStatusEnum.SUCCESS == task.getStatus())
                .max(Comparator.comparing(Task::getEndTime)).orElse(new Task());

        if (tempTask.getEndTime() == null) {
            return false;
        }

        if (updateTime != null) {
            if (tempTask.getEndTime().before(updateTime)) {
                return false;
            }
        }

        long dur = (tempTask.getEndTime().getTime() - tempTask.getExecTime().getTime()) / 1_000;
        return dur <= JobConstant.CHECK_TEMP_TASK_DUR_MAX;
    }
}
