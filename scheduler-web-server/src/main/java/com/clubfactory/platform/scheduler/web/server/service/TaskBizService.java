package com.clubfactory.platform.scheduler.web.server.service;

import com.clubfactory.platform.scheduler.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.common.constant.DateFormatPattern;
import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.common.util.DateUtil;
import com.clubfactory.platform.scheduler.dal.enums.TaskStatusEnum;
import com.clubfactory.platform.scheduler.dal.po.*;
import com.clubfactory.platform.scheduler.web.core.constant.Fields;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.service.*;
import com.clubfactory.platform.scheduler.web.core.vo.TaskVO;
import com.clubfactory.platform.scheduler.web.server.dto.OverviewDto;
import com.clubfactory.platform.scheduler.web.server.dto.TaskQueryDto;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.service.inter.TaskPageCallback;
import com.clubfactory.platform.scheduler.web.server.vo.SortByVo;
import com.clubfactory.platform.scheduler.web.server.vo.TaskOverviewVo;
import com.clubfactory.platform.scheduler.web.server.vo.TaskQueryVo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Service
public class TaskBizService {

    @Autowired
    private TaskStatisticService taskStatisticService;
    @Autowired
    UserService userService;
    @Autowired
    TaskService taskService;
    @Autowired
    ScriptService scriptService;
    @Autowired
    TaskDependsService taskDependsService;
    @Autowired
    MachineService machineService;
    @Autowired
    JobService jobService;
    @Autowired
	TaskMonitorService taskMonitorService;
    @Autowired
	JobOnlineService jobOnlineService;
    
    // 递归最大循环次数
    private static final int MAX_DEPENDS_NUM = 100_000;

    public String getYarnAppId(Long taskId) {
		TaskMonitor taskMonitor = taskMonitorService.getByTaskId(taskId);
		if (taskMonitor == null) {
			return null;
		}
		return taskMonitor.getYarnAppId();
	}

    @Transactional
    public Boolean reRunAll(List<Long> ids, LoginUserDto userDto) {
    	Assert.collectionNotEmpty(ids, "ids");
    	
    	checkTaskPermission(ids, userDto);
    	
    	initTask(ids, userDto);

    	List<Long> allIds = listChildIds(ids);

    	if (CollectionUtils.isEmpty(allIds)) {
    		throw new BizException(ErrorCode.NO_CHILD_TASK);
		}
    	
    	return taskService.editInitByIdsWithoutDate(allIds, userDto.getLocalUserId());
    }

	private List<Long> listChildIds(List<Long> ids) {
		Set<Long> setAllIds = Sets.newHashSet();
    	for (int i = 0; i < MAX_DEPENDS_NUM; i++) {
	    	List<Long> reIds = taskDependsService.listIdsByParentIds(ids);
	    	if (CollectionUtils.isEmpty(reIds)) {
	    		break;
	    	}
	    	setAllIds.addAll(reIds);
	    	ids = reIds;
    	}
		return Lists.newArrayList(setAllIds);
	}
    
    @Transactional
    public Boolean reRun(List<Long> ids, LoginUserDto userDto) {
    	Assert.collectionNotEmpty(ids, "ids");
    	
    	checkTaskPermission(ids, userDto);
    	
    	initTask(ids, userDto);

    	return true;
    }
    
    
    public Boolean kill(List<Long> ids, LoginUserDto userDto) {
    	Assert.collectionNotEmpty(ids, "ids");
    	
    	checkTaskPermission(ids, userDto);
    	
    	List<TaskVO> taskVOs = taskService.listByIds(ids, userDto.getLocalUserId(), userDto.getIsAdmin());
    	if (CollectionUtils.isEmpty(taskVOs)) {
    		return false;
    	}
  
    	List<Long> updateIds = listIdByTasks(userDto, taskVOs);
    	// 更新
    	// 通过scheduler定时扫，去操作
    	return taskService.killByIds(updateIds);
    }
    
    public Boolean editSuccess(List<Long> ids, LoginUserDto userDto) {
    	Assert.collectionNotEmpty(ids, "ids");
    	
    	checkTaskPermission(ids, userDto);
    	
    	List<TaskVO> taskVOs = taskService.listByIds(ids, userDto.getLocalUserId(), userDto.getIsAdmin());
    	if (CollectionUtils.isEmpty(taskVOs)) {
    		return false;
    	}
    	
    	// 正在跑的，强制置为成功
    	List<TaskVO> runTasks = taskVOs.stream().filter(task -> task.getStatus() == TaskStatusEnum.RUNNING).collect(Collectors.toList());
    	if (CollectionUtils.isNotEmpty(runTasks)) {
    		List<Long> taskIds = runTasks.stream().map(TaskVO::getId).collect(Collectors.toList());
    		taskService.editSuccessByIds(taskIds, TaskStatusEnum.MANUAL_SUCCESS, userDto.getLocalUserId());
    	}
    	// 其他状态改为成功
    	List<TaskVO> notRunTasks = taskVOs.stream().filter(task -> task.getStatus() != TaskStatusEnum.RUNNING).collect(Collectors.toList());
    	if (CollectionUtils.isNotEmpty(notRunTasks)) {
    		List<Long> taskIds = notRunTasks.stream().map(TaskVO::getId).collect(Collectors.toList());
    		taskService.editSuccessByIds(taskIds, TaskStatusEnum.SUCCESS, userDto.getLocalUserId());
    	}
    	
    	return true;
    }
    
    public Boolean regain(List<Long> ids, LoginUserDto userDto) {
    	Assert.collectionNotEmpty(ids, "ids");
    	
    	checkTaskPermission(ids, userDto);
    	// 更新
    	return taskService.regainByIds(ids);
    }

	private void checkTaskPermission(List<Long> ids, LoginUserDto userDto) {
		if (BooleanUtils.isFalse(userDto.getIsAdmin())) {
    		List<TaskVO> taskVOs = taskService.listAllByIds(ids);
    		List<Long> jobIds = taskVOs.stream().map(TaskVO::getJobId).collect(Collectors.toList());
    		List<Job> jobs = jobService.listByIds(jobIds);
    		for (Job job : jobs) {
    			if (!job.getCreateUser().equals(userDto.getLocalUserId())) {
    				throw new BizException(ErrorCode.TASK_NOT_PERMISSION);
    			}
    		}
		}
	}

	private void initTask(List<Long> ids, LoginUserDto userDto) {
		List<TaskVO> taskVOs = taskService.listAllByIds(ids);
    	if (CollectionUtils.isEmpty(taskVOs)) {
    		return;
    	}

		List<TaskStatusEnum> enums = Lists.newArrayList();
		enums.add(TaskStatusEnum.RUNNING);
		enums.add(TaskStatusEnum.KILLING);
		enums.add(TaskStatusEnum.READY);
		enums.add(TaskStatusEnum.SCHEDULED);
    	for (TaskVO task : taskVOs) {
    		if (enums.contains(task.getStatus())) {
    			throw new BizException(ErrorCode.TASK_NOT_ALLOW_RERUN.setParams(task.getId().toString()));
    		}
    	}
    	
    	// 临时节点用最新的脚本
    	this.editTempTaskScirpt(taskVOs, userDto);
    	
    	// 初始化实例，以及它的dqc实例
		updateInitTask(ids, userDto);
		return;
	}

	private void updateInitTask(List<Long> ids, LoginUserDto userDto) {
		taskService.editInitByIds(ids,  userDto.getLocalUserId());
		List<Long> childs = taskDependsService.listIdsByParentIds(ids);
		if (CollectionUtils.isEmpty(childs)) {
			return;
		}
		List<Long> dqcChilds = taskService.listDqcByIds(childs).stream().map(TaskVO::getId).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(dqcChilds)) {
			return;
		}
		taskService.editInitByIds(dqcChilds,  userDto.getLocalUserId());
	}

	private void editTempTaskScirpt(List<TaskVO> taskVOs, LoginUserDto userDto) {
		List<Long> tempJobIds = taskVOs.stream().filter(task -> BooleanUtils.isTrue(task.getIsTemp())).map(TaskVO::getJobId).distinct().collect(Collectors.toList());
    	if (CollectionUtils.isNotEmpty(tempJobIds)) {
    		List<Job> jobs = jobService.listByIds(tempJobIds);
    		Map<Long, Long> scriptMap = jobs.stream().collect(Collectors.toMap(Job::getId, Job::getScriptId));
    		for (TaskVO taskVO : taskVOs) {
    			if (BooleanUtils.isNotTrue(taskVO.getIsTemp())) {
    				continue;
    			}
    			Long scriptId = scriptMap.get(taskVO.getJobId());
				if (scriptId != null && !taskVO.getScriptId().equals(scriptId)) {
    				taskService.editScriptId(taskVO.getId(), scriptId, userDto.getLocalUserId(), userDto.getIsAdmin());
    			}
    		}
    	}
	}

	private List<Long> listIdByTasks(LoginUserDto userDto, List<TaskVO> taskVOs) {
		List<Long> updateIds = taskVOs.stream()
    		.map(TaskVO::getId)
    		.collect(Collectors.toList());
		return updateIds;
	}
	
	public PageUtils<TaskQueryVo> queryDelayPage(TaskQueryDto queryDto, LoginUserDto userDto) {
		PageUtils<TaskQueryVo> pageUtils = this.queryBasePage(queryDto, userDto, new TaskPageCallback() {

			@Override
			public PageUtils<Task> doInPageList(Task task) {
				return taskService.pageDelayList(task);
			}
			
		});
		return pageUtils;
	}
	
	public PageUtils<TaskQueryVo> queryFailedPage(TaskQueryDto queryDto, LoginUserDto userDto) {
		PageUtils<TaskQueryVo> pageUtils = this.queryBasePage(queryDto, userDto, new TaskPageCallback() {

			@Override
			public PageUtils<Task> doInPageList(Task task) {
				task.setStatus(TaskStatusEnum.FAILED);
				return taskService.pageList(task);
			}
			
		});
		return pageUtils;
	}
	
	public PageUtils<TaskQueryVo> queryPage(TaskQueryDto queryDto, LoginUserDto userDto) {
		PageUtils<TaskQueryVo> pageUtils = this.queryBasePage(queryDto, userDto, new TaskPageCallback() {

			@Override
			public PageUtils<Task> doInPageList(Task task) {
				return taskService.pageList(task);
			}
			
		});
		return pageUtils;
	}
    
    private PageUtils<TaskQueryVo> queryBasePage(TaskQueryDto queryDto, LoginUserDto userDto, TaskPageCallback iTaskBizService) {
    	Assert.notNull(queryDto);
    	Assert.notNull(queryDto.getPageNo());
		Assert.notNull(queryDto.getPageSize());
		
		Task task = new Task();
		task.setStartDate(queryDto.getStartTime());
		String endDate = getEndDate(queryDto.getEndTime());
		task.setEndDate(endDate);
		task.setId(queryDto.getId());
		task.setJobId(queryDto.getJobId());
		//task.setJobType(JobTypeEnum.NORMAL);
		task.setName(queryDto.getName());
		task.setType(queryDto.getType());
		task.setPageNo(queryDto.getPageNo());
		task.setPageSize(queryDto.getPageSize());
		task.setStatus(queryDto.getStatus());
		task.setCategory(queryDto.getCategory());
		task.setStatuses(queryDto.getStatuses());
		task.setIsDeleted(false);
		
		if (StringUtils.isNotBlank(queryDto.getUserName()) || StringUtils.isNotBlank(queryDto.getDepartName())) {
			List<Long> userIds = userService.listIdsByUserNameAndDepartName(queryDto.getUserName(), queryDto.getDepartName());
			if (CollectionUtils.isEmpty(userIds)) {
				return new PageUtils<TaskQueryVo>(Lists.newArrayList(), 0, queryDto.getPageSize(), queryDto.getPageNo());
			}
			task.setIds(userIds);
			task.setQueryListFieldName("create_user");
		}
		String orderByString = "update_time desc, start_time desc, task_time desc";
		if (CollectionUtils.isNotEmpty(queryDto.getSortByList())) {
			orderByString = getOrderFromParam(queryDto.getSortByList());
		}
		task.setOrderBy(orderByString);
		PageUtils<Task> pageVo =iTaskBizService.doInPageList(task);
		// 返回数据
		if (pageVo.getSize() == 0) {
			return new PageUtils<TaskQueryVo>(queryDto.getPageSize(), queryDto.getPageNo());
		}

		List<Task> tasks = pageVo.getRows();
		Map<Long, Integer> versionMap = jobOnlineService.getVersionMapByJobIds(tasks.stream().map(Task::getJobId).collect(Collectors.toList()));
		Map<Long, Script> idScriptMap = getIdScriptMap(pageVo);
		List<TaskQueryVo> taskQueryVos = tasks.stream().map(vo -> {
			TaskQueryVo taskQueryVo = new TaskQueryVo();
			taskQueryVo.setId(vo.getId());
			taskQueryVo.setJobType(vo.getType());
			taskQueryVo.setCategory(vo.getCategory());
			taskQueryVo.setJobId(vo.getJobId());
			taskQueryVo.setName(vo.getName());
			taskQueryVo.setTypeDesc(vo.getCategory().getDesc() + " | " + vo.getType());
			taskQueryVo.setUserName(userService.getUserName(vo.getCreateUser()));
			taskQueryVo.setDepartName(userService.getDepartName(vo.getCreateUser()));
			taskQueryVo.setStartTimeStr(DateUtil.format(vo.getStartTime(), DateFormatPattern.YYYY_MM_DD_HH_MM_SS));
			if (vo.getExecTime() != null) {
				taskQueryVo.setExecTimeStr(DateUtil.format(vo.getExecTime(), DateFormatPattern.YYYY_MM_DD_HH_MM_SS));
			}
			taskQueryVo.setTaskTimeStr(DateUtil.format(vo.getTaskTime(), DateFormatPattern.YYYY_MM_DD_HH_MM_SS));
			taskQueryVo.setIsOnline(!vo.getIsTemp());
			taskQueryVo.setStatusDesc(vo.getStatus().getDesc());
			taskQueryVo.setRetryCount(vo.getRetryCount());
			taskQueryVo.setRetryMax(vo.getRetryMax());
			taskQueryVo.setUpdateTime(vo.getUpdateTime());
			taskQueryVo.setEndTimeStr(DateUtil.format(vo.getEndTime(), DateFormatPattern.YYYY_MM_DD_HH_MM_SS));
			if (vo.getEndTime() != null 
					&& vo.getExecTime() != null) {
				long dur = (vo.getEndTime().getTime() - vo.getExecTime().getTime())/1000;
				taskQueryVo.setDur(dur);
			}
			taskQueryVo.setMachineName(Optional.ofNullable(vo.getIp()).orElse("系统随机"));
			taskQueryVo.setIsProduce(BooleanUtils.isTrue(vo.getIsTemp()) ? "否" : "是");
			Script script = idScriptMap.get(vo.getScriptId());
			if (script != null) {
				taskQueryVo.setScriptId(String.valueOf(script.getId()));
				taskQueryVo.setScriptName(script.getScriptName());
				Integer version = getVersion(versionMap, vo, script);
				taskQueryVo.setScriptVersion(version);
			}
			return taskQueryVo;
		}).collect(Collectors.toList());
		
    	
		return new PageUtils<TaskQueryVo>(taskQueryVos, pageVo.getTotalCount(), queryDto.getPageSize(), queryDto.getPageNo());
    }

	private Integer getVersion(Map<Long, Integer> versionMap, Task vo, Script script) {
		Integer version = null;
		if (org.apache.commons.lang3.BooleanUtils.isTrue(vo.getIsTemp())) {
			version = script.getVersion();
		} else {
			version = versionMap.get(vo.getJobId());
		}
		return version;
	}


	private String getEndDate(String endTime) {
		if (StringUtils.isEmpty(endTime)) {
			return null;
		}
		if (endTime.length() == DateFormatPattern.YYYY_MM_DD.length()) {
			int num = 24;
			int day = DateUtil.parse(endTime, DateFormatPattern.YYYY_MM_DD).getDay();
			if (day == new Date().getDay()) {
				num = LocalDateTime.now().plusHours(1).getHour();
			}
			return format(endTime, DateFormatPattern.YYYY_MM_DD, num);
		} else if (endTime.length() == DateFormatPattern.YYYY_MM_DD_HH.length()) {
			return format(endTime, DateFormatPattern.YYYY_MM_DD_HH, 1);
		} else if (endTime.length() == DateFormatPattern.YYYY_MM_DD_HH_MM.length()) {
			return format(endTime, DateFormatPattern.YYYY_MM_DD_HH_MM, 1);
		} else if (endTime.length() == DateFormatPattern.YYYY_MM_DD_HH_MM_SS.length()) {
			return format(endTime, DateFormatPattern.YYYY_MM_DD_HH_MM_SS, 1);
		} 
		return endTime;
	}

	private String format(String endTime, String format, int num) {
		return DateUtil.format(DateUtil.getLocalDateTime(DateUtil.parse(endTime, format)).plusHours(num), DateFormatPattern.YYYY_MM_DD_HH_MM_SS);
	}

	private Map<Long, Script> getIdScriptMap(PageUtils<Task> pageVo) {
		List<Long> ids = pageVo.getRows().stream().map(vo -> vo.getScriptId()).collect(Collectors.toList());
		Map<Long, Script> idScriptMap = new HashMap<>();
		List<Script> scripts =  scriptService.listPoByField(Fields.ID, ids);
		for (Script script : scripts) {
			idScriptMap.put(script.getId(), script);
		}
		return idScriptMap;
	}


	/**
	 * 分页查询task运行概览信息
	 * @param queryDto
	 * @return
	 */
    public PageUtils<TaskOverviewVo> queryPage(OverviewDto queryDto){
        Assert.notNull(queryDto);
        Assert.notNull(queryDto.getStatisticDim());
        TaskStatistic taskStatistic = new TaskStatistic();
        taskStatistic.setPageNo(queryDto.getPageNo());
        taskStatistic.setPageSize(queryDto.getPageSize());
        taskStatistic.setStatisticDim(queryDto.getStatisticDim());
        taskStatistic.setStartDate(queryDto.getStartTime());
        taskStatistic.setEndDate(queryDto.getEndTime());

        PageUtils<TaskStatistic> pageUtils = taskStatisticService.pageList(taskStatistic);

        if (pageUtils.getSize() == 0) {
            return new PageUtils<TaskOverviewVo>();
        }

        List<TaskOverviewVo> taskOverviewVos = pageUtils.getRows()
                .stream()
                .map( vo -> {
                    TaskOverviewVo taskOverviewVo = new TaskOverviewVo();
                    taskOverviewVo.setDelayTaskCount(vo.getDelayTaskCount());
                    taskOverviewVo.setDepartId(vo.getDepartId());
                    taskOverviewVo.setDepartName(vo.getDepartName());
                    taskOverviewVo.setFailedTaskCount(vo.getFailedTaskCount());
                    taskOverviewVo.setSucceedTaskCount(vo.getSucceedTaskCount());
                    taskOverviewVo.setSucceedRate(vo.getSucceedRate() + "%");
                    taskOverviewVo.setTotalTask(vo.getTotalTask());
                    taskOverviewVo.setType(vo.getType());
                    taskOverviewVo.setTaskDate(DateUtil.format(vo.getTaskDate(),"yyyy-MM-dd"));
                    taskOverviewVo.setId(vo.getId());

                    return taskOverviewVo;
                }).collect(Collectors.toList());
        return new PageUtils<TaskOverviewVo>(taskOverviewVos, pageUtils.getTotalCount(), queryDto.getPageSize(), queryDto.getPageNo());
    }


	private String getOrderFromParam(List<SortByVo> sortByList) {
		if (CollectionUtils.isEmpty(sortByList)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (SortByVo sortByVo : sortByList) {
			String fieldName = sortByVo.getFieldName();
			switch (fieldName) {
				case "jobId":
					fieldName = Fields.JOB_ID; break;
				case "taskTimeStr":
					fieldName = Fields.TASK_TIME; break;
				case "startTimeStr":
					fieldName = Fields.START_TIME; break;
				case "machineName":
					fieldName = Fields.IP; break;
			}
			sb.append(fieldName)
					.append(" ")
					.append(sortByVo.getSortType())
					.append(", ");
		}
		String orderBy = sb.substring(0, sb.length() - 2);
		return orderBy;
	}

}
