package com.clubfactory.platform.scheduler.web.server.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.scheduler.common.utils.TaskDependsUtil;
import com.clubfactory.platform.scheduler.dal.enums.DependTypeEnum;
import com.clubfactory.platform.scheduler.dal.po.JobOnline;
import com.clubfactory.platform.scheduler.dal.po.Task;
import com.clubfactory.platform.scheduler.dal.po.TaskDepends;
import com.clubfactory.platform.scheduler.web.core.service.JobOnlineDependsService;
import com.clubfactory.platform.scheduler.web.core.service.JobOnlineService;
import com.clubfactory.platform.scheduler.web.core.service.TaskDependsService;
import com.clubfactory.platform.scheduler.web.core.service.TaskService;
import com.clubfactory.platform.scheduler.web.core.vo.JobOnlineDependsVO;
import com.google.common.collect.Lists;

@Service
public class TaskDependsBizService {
	
	@Resource
	TaskDependsService taskDependsService;
	@Resource
	JobOnlineDependsService jobOnlineDependsService;
	@Resource
	TaskService taskService;
	@Resource
	JobOnlineService jobOnlineService;

	public void saveDepends(List<Task> tasks, JobOnline jobOnline) {
		// 保存依赖
		List<JobOnlineDependsVO> jobOnlineDependsVOs = jobOnlineDependsService.listByJobId(jobOnline.getJobId());
		List<JobOnlineDependsVO> subJobOnlineDependsVOs = jobOnlineDependsService.listByParentId(jobOnline.getJobId());
		// 自依赖
		if (CollectionUtils.isNotEmpty(jobOnlineDependsVOs)) {
			genSelf(tasks, jobOnline, jobOnlineDependsVOs);
		}
		// 我继承别人
		if (CollectionUtils.isNotEmpty(jobOnlineDependsVOs)) {
			genParentJobDepends(tasks, jobOnline, jobOnlineDependsVOs);
		}
		// 别人继承我
		if (CollectionUtils.isNotEmpty(subJobOnlineDependsVOs)) {
			genSubJobDepends(tasks, jobOnline, subJobOnlineDependsVOs);
		}
	}
	
	private void genSelf(List<Task> tasks, JobOnline jobOnline, List<JobOnlineDependsVO> jobOnlineDependsVOs) {
		Map<DependTypeEnum, List<JobOnlineDependsVO>> dependsMap = getDependsMap(jobOnlineDependsVOs);
		// 不管谁继承谁，自依赖都只有一个
		if (CollectionUtils.isNotEmpty(dependsMap.get(DependTypeEnum.SELF))) {
			List<Task> parentTasks = listExistTasks(jobOnline, tasks);
			List<TaskDepends> taskDepends = TaskDependsUtil.genSelfDependsByJob(jobOnline, tasks, parentTasks);
			taskDependsService.saveBatch(taskDepends);
		};
	}

	private void genSubJobDepends(List<Task> tasks, JobOnline jobOnline, List<JobOnlineDependsVO> subJobOnlineDependsVOs) {
		List<TaskDepends> taskDepends = Lists.newArrayList();
		Map<DependTypeEnum, List<JobOnlineDependsVO>> subDependsMap = getDependsMap(subJobOnlineDependsVOs);
		if (CollectionUtils.isNotEmpty(subDependsMap.get(DependTypeEnum.SAME))) {
			List<TaskDepends> subTaskDepends = this.genSubSameDepends(jobOnline, subDependsMap.get(DependTypeEnum.SAME), tasks);
			taskDepends.addAll(subTaskDepends);
		}
		if (CollectionUtils.isNotEmpty(subDependsMap.get(DependTypeEnum.ALL))) {
			List<TaskDepends> subTaskDepends = this.genSubAllDepends(jobOnline, subDependsMap.get(DependTypeEnum.ALL), tasks);
			taskDepends.addAll(subTaskDepends);
		}
		if (CollectionUtils.isNotEmpty(subDependsMap.get(DependTypeEnum.PREV))) {
			List<TaskDepends> subTaskDepends = this.genSubPrevDepends(jobOnline, subDependsMap.get(DependTypeEnum.PREV), tasks);
			taskDepends.addAll(subTaskDepends);
		}
		if (CollectionUtils.isNotEmpty(taskDepends)) {
			taskDependsService.saveBatch(taskDepends);
		}
	}

	private void genParentJobDepends(List<Task> tasks, JobOnline jobOnline, List<JobOnlineDependsVO> jobOnlineDependsVOs) {
		List<TaskDepends> taskDepends = Lists.newArrayList();
		Map<DependTypeEnum, List<JobOnlineDependsVO>> dependsMap = getDependsMap(jobOnlineDependsVOs);
		if (CollectionUtils.isNotEmpty(dependsMap.get(DependTypeEnum.SAME))) {
			List<JobOnlineDependsVO> jobOnlineDepends = dependsMap.get(DependTypeEnum.SAME);
			List<TaskDepends> subTaskDepends =  this.genSameDepends(jobOnline, jobOnlineDepends, tasks);
			taskDepends.addAll(subTaskDepends);
		}
		if (CollectionUtils.isNotEmpty(dependsMap.get(DependTypeEnum.ALL))) {
			List<JobOnlineDependsVO> jobOnlineDepends = dependsMap.get(DependTypeEnum.ALL);
			List<TaskDepends> subTaskDepends =  this.genAllDepends(jobOnline, jobOnlineDepends, tasks);
			taskDepends.addAll(subTaskDepends);
		}
		if (CollectionUtils.isNotEmpty(dependsMap.get(DependTypeEnum.PREV))) {
			List<JobOnlineDependsVO> jobOnlineDepends = dependsMap.get(DependTypeEnum.PREV);
			List<TaskDepends> subTaskDepends =  this.genPrevDepends(jobOnline, jobOnlineDepends, tasks);
			taskDepends.addAll(subTaskDepends);
		}
		if (CollectionUtils.isNotEmpty(taskDepends)) {
			taskDependsService.saveBatch(taskDepends);
		}
	}

	private Map<DependTypeEnum, List<JobOnlineDependsVO>> getDependsMap(List<JobOnlineDependsVO> jobOnlineDependsVOs) {
		return jobOnlineDependsVOs.stream().collect(Collectors.groupingBy(JobOnlineDependsVO::getType));
	}
	
	private List<TaskDepends> genSubAllDepends(JobOnline jobOnline, List<JobOnlineDependsVO> jobOnlineDepends, List<Task> tasks) {
		List<Long> jobIds = listJobIdByDepends(jobOnline, jobOnlineDepends, false);
		Map<Long, JobOnline> jobMap = jobOnlineService.getMapByIds(jobIds);
		
		List<TaskDepends> taskDepends = Lists.newArrayList();
		// 子实例列表
		List<Task> existTasks = taskService.listByJobIds(jobIds, false);
		for (JobOnlineDependsVO dependsVO : jobOnlineDepends) {
			JobOnline job = jobMap.get(dependsVO.getJobId());
			taskDepends = TaskDependsUtil.genAllDependsByCycleType(job, jobOnline, existTasks, tasks);
		}
		return taskDepends;
	}

	private List<TaskDepends> genAllDepends(JobOnline jobOnline, List<JobOnlineDependsVO> jobOnlineDepends, List<Task> tasks) {
		List<Long> parentIds = listJobIdByDepends(jobOnline, jobOnlineDepends, true);
		Map<Long, JobOnline> parentJobMap = jobOnlineService.getMapByIds(parentIds);
		
		List<TaskDepends> taskDepends = Lists.newArrayList();
		List<Task> existTasks = taskService.listByJobIds(parentIds, false);
		for (JobOnlineDependsVO dependsVO : jobOnlineDepends) {
			JobOnline parentJob = parentJobMap.get(dependsVO.getParentId());
			taskDepends = TaskDependsUtil.genAllDependsByCycleType(jobOnline, parentJob, tasks, existTasks);
		}
		return taskDepends;
	}

	private List<TaskDepends> genSubPrevDepends(JobOnline jobOnline, List<JobOnlineDependsVO> jobOnlineDepends, List<Task> tasks) {
		List<Long> jobIds = listJobIdByDepends(jobOnline, jobOnlineDepends, false);
		Map<Long, JobOnline> jobMap = jobOnlineService.getMapByIds(jobIds);

		List<TaskDepends> taskDepends = Lists.newArrayList();
		// 子实例列表
		List<Task> existTasks = taskService.listByJobIds(jobIds, false);
		for (JobOnlineDependsVO dependsVO : jobOnlineDepends) {
			JobOnline job = jobMap.get(dependsVO.getJobId());
			taskDepends = TaskDependsUtil.genPrevDependsByCycleType(job, jobOnline, existTasks, tasks);
		}
		return taskDepends;
	}

	private List<TaskDepends> genPrevDepends(JobOnline jobOnline, List<JobOnlineDependsVO> jobOnlineDepends, List<Task> tasks) {
		List<Long> parentIds = listJobIdByDepends(jobOnline, jobOnlineDepends, true);
		Map<Long, JobOnline> parentJobMap = jobOnlineService.getMapByIds(parentIds);

		List<TaskDepends> taskDepends = Lists.newArrayList();
		List<Task> existTasks = taskService.listByJobIds(parentIds, false);
		for (JobOnlineDependsVO dependsVO : jobOnlineDepends) {
			JobOnline parentJob = parentJobMap.get(dependsVO.getParentId());
			taskDepends = TaskDependsUtil.genPrevDependsByCycleType(jobOnline, parentJob, tasks, existTasks);
		}
		return taskDepends;
	}

	private List<Long> listJobIdByDepends(JobOnline jobOnline, List<JobOnlineDependsVO> jobOnlineDepends, Boolean isParent) {
		Assert.notNull(jobOnline);
		if (CollectionUtils.isEmpty(jobOnlineDepends)) {
			return Lists.newArrayList();
		}
		List<Long> jobIds = BooleanUtils.isTrue(isParent) 
				? jobOnlineDepends.stream().map(JobOnlineDependsVO::getParentId).collect(Collectors.toList())
				: jobOnlineDepends.stream().map(JobOnlineDependsVO::getJobId).collect(Collectors.toList());
		jobIds.remove(jobOnline.getJobId());
		return jobIds;
	}

	// 子是本节点
	private List<TaskDepends> genSubSameDepends(JobOnline jobOnline, List<JobOnlineDependsVO> jobOnlineDepends, List<Task> tasks) {
		List<Long> jobIds = listJobIdByDepends(jobOnline, jobOnlineDepends, false);
		Map<Long, JobOnline> jobMap = jobOnlineService.getMapByIds(jobIds);

		List<TaskDepends> taskDepends = Lists.newArrayList();
		List<Task> existTasks = taskService.listByJobIds(jobIds, false);
		for (JobOnlineDependsVO dependsVO : jobOnlineDepends) {
			JobOnline job = jobMap.get(dependsVO.getJobId());
			taskDepends = TaskDependsUtil.genSameDependsByCycleType(job, jobOnline, existTasks, tasks);
		}
		return taskDepends;
	}

	// 子是本节点
	private List<TaskDepends> genSameDepends(JobOnline jobOnline, List<JobOnlineDependsVO> jobOnlineDepends, List<Task> tasks) {
		List<Long> parentIds = listJobIdByDepends(jobOnline, jobOnlineDepends, true);
		Map<Long, JobOnline> parentJobMap = jobOnlineService.getMapByIds(parentIds);
		List<Task> existTasks = taskService.listByJobIds(parentIds, false);
		List<TaskDepends> taskDepends = Lists.newArrayList();
		for (JobOnlineDependsVO dependsVO : jobOnlineDepends) {
			JobOnline job = parentJobMap.get(dependsVO.getParentId());
			taskDepends = TaskDependsUtil.genAllDependsByCycleType(jobOnline, job, tasks,  existTasks);
		}
		return taskDepends;
	}

	private List<Task> listExistTasks(JobOnline jobOnline, List<Task> tasks) {
		List<Task> existTasks = taskService.listByJobId(jobOnline.getJobId());
		List<Long> taskIds = tasks.stream().map(Task::getId).collect(Collectors.toList());
		Iterator<Task> iterator = existTasks.iterator();
		while(iterator.hasNext()) {
			Task taskVo = iterator.next();
			if (taskIds.contains(taskVo.getId())) {
				iterator.remove();
			}
		}
		return existTasks;
	}

}
