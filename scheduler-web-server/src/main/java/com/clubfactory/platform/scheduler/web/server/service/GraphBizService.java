package com.clubfactory.platform.scheduler.web.server.service;

import com.alibaba.fastjson.JSON;
import com.clubfactory.platform.scheduler.common.constant.DateFormatPattern;
import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.common.util.DateUtil;
import com.clubfactory.platform.scheduler.dal.utils.TaskDependsUtil;
import com.clubfactory.platform.scheduler.dal.dto.SchedulerTimeDto;
import com.clubfactory.platform.scheduler.dal.enums.DependTypeEnum;
import com.clubfactory.platform.scheduler.dal.enums.JobCycleTypeEnum;
import com.clubfactory.platform.scheduler.dal.enums.JobStatusEnum;
import com.clubfactory.platform.scheduler.dal.po.*;
import com.clubfactory.platform.scheduler.web.core.dto.GraphDto;
import com.clubfactory.platform.scheduler.web.core.dto.JobExtCommonDto;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.enums.GraphThendType;
import com.clubfactory.platform.scheduler.web.core.service.*;
import com.clubfactory.platform.scheduler.web.core.utils.JobDependsCycleCheck;
import com.clubfactory.platform.scheduler.web.core.vo.*;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.vo.JobEnumVo.Content;
import com.clubfactory.platform.scheduler.web.server.vo.Vertex;
import com.clubfactory.platform.scheduler.web.server.vo.Vertex.Edge;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GraphBizService {

	@Resource
	JobService jobService;
	@Resource
	JobDependsService jobDependsService;
	@Resource
	JobOnlineDependsService jobOnlineDependsService;
	@Resource
	JobOnlineService jobOnlineService;
	@Resource
	TaskService taskService;
	@Resource
	JobBizService jobBizService;
	@Resource
	TaskDependsService taskDependsService;
	@Transactional
	public Boolean editGrpah(GraphDto graphDto,LoginUserDto userDto) {
		Assert.notNull(graphDto);
		Long id = graphDto.getId();
		Assert.notNull(id);
		
		log.info("jobDepends edit, name:{}, job_id:{}, graph:{}",userDto.getName(), id, JSON.toJSONString(graphDto, true));
		List<GraphDto> parents = Optional.ofNullable(graphDto.getParents()).orElse(Lists.newArrayList());
		// 检查是否允许建立依赖
		checkDepends(id, parents);
		
		// 如果任务处于线上状态
		doOnJobDepends(graphDto, userDto);
		// 线下配依赖功能废除
		//doOffJobDepends(graphDto, userDto);
		
		return true;
	}

	private void doOnJobDepends(GraphDto graphDto, LoginUserDto userDto) {
		Long id = graphDto.getId();
		JobOnline jobOnline = jobOnlineService.getById(id);
		List<Task> tasks = removeOnlineDepends(id);
		saveJobOnlineDepends(graphDto, userDto, id);
		saveTaskDepends(jobOnline, graphDto, tasks);
	}

	private void saveTaskDepends(JobOnline jobOnline, GraphDto graphDto, List<Task> tasks) {
		if (CollectionUtils.isEmpty(tasks)) {
			return;
		}
		
		List<GraphDto> parents = graphDto.getParents();
		// 自依赖
		if (BooleanUtils.isTrue(graphDto.getIsSelfDependent())) {
			List<TaskDepends> taskDepends = Lists.newArrayList();
			for (Task task : tasks) {
				List<TaskDepends> subTaskDepends = TaskDependsUtil.genSelfDependsByJob(jobOnline, Lists.newArrayList(task), tasks);	
				taskDepends.addAll(subTaskDepends);
			}
			taskDependsService.saveBatch(taskDepends);
		}
		// 同周期，全周期
		if (CollectionUtils.isNotEmpty(parents)) {
			List<Long> parentsIds = parents.stream().map(GraphDto::getId).collect(Collectors.toList());
			Map<Long, JobOnline> jobOnlineMap = jobOnlineService.getMapByIds(parentsIds);
			
			Map<Long, List<Task>> parentTasksMap = taskService.getMapByJobIds(parentsIds);
			List<TaskDepends> taskDepends = Lists.newArrayList();
			for (GraphDto parent : parents) {
				JobOnline parentJobOnline = jobOnlineMap.get(parent.getId());
				List<Task> parentTasks = parentTasksMap.get(parent.getId());
				if (DependTypeEnum.ALL == parent.getType()) {
					List<TaskDepends> subTaskDepends = TaskDependsUtil.genAllDependsByCycleType(jobOnline, parentJobOnline, tasks,  parentTasks);
					taskDepends.addAll(subTaskDepends);
				}
				if (DependTypeEnum.SAME == parent.getType()) {
					List<TaskDepends> subTaskDepends = TaskDependsUtil.genSameDependsByCycleType(jobOnline, parentJobOnline, tasks, parentTasks);
					taskDepends.addAll(subTaskDepends);
				}
				if (DependTypeEnum.PREV == parent.getType()) {
					List<TaskDepends> subTaskDepends = TaskDependsUtil.genPrevDependsByCycleType(jobOnline, parentJobOnline, tasks, parentTasks);
					taskDepends.addAll(subTaskDepends);
				}
			}
			taskDependsService.saveBatch(taskDepends);
		}
	}

	private void saveJobOnlineDepends(GraphDto graphDto, LoginUserDto userDto, Long id) {
		List<GraphDto> parents = graphDto.getParents();
		// 保存自依赖信息
		if (BooleanUtils.isTrue(graphDto.getIsSelfDependent())) {
			JobOnlineDepends depends = new JobOnlineDepends();
			depends.setJobId(id);
			depends.setParentId(id);
			depends.setCreateUser(userDto.getLocalUserId());
			depends.setUpdateUser(userDto.getLocalUserId());
			depends.setType(DependTypeEnum.SELF);
			jobOnlineDependsService.save(depends);
		}
		
		// 保存其他节点依赖信息
		if (CollectionUtils.isNotEmpty(parents)) {
			List<JobOnlineDepends> dependses = parents.stream().map(graph -> {
				JobOnlineDepends depends = new JobOnlineDepends();
				depends.setJobId(id);
				depends.setParentId(graph.getId());
				depends.setType(graph.getType());
				depends.setCreateUser(userDto.getLocalUserId());
				depends.setUpdateUser(userDto.getLocalUserId());
				return depends;
			}).collect(Collectors.toList());
			jobOnlineDependsService.saveBatch(dependses);
		}
	}

	private List<Task> removeOnlineDepends(Long id) {
		// 删除线上任务依赖
		jobOnlineDependsService.removeByJobId(id);
		// 删除task依赖
		List<Task> tasks = taskService.listByJobId(id);
		if (CollectionUtils.isNotEmpty(tasks)) {
			List<Long> taskIds = tasks.stream().map(Task::getId).collect(Collectors.toList());
			taskDependsService.removeByIds("task_id", taskIds);
		}
		return tasks;
	}

	private void doOffJobDepends(GraphDto graphDto, LoginUserDto userDto) {
		Long id = graphDto.getId();
		List<GraphDto> parents = graphDto.getParents();
		// 物理删除老依赖
		jobDependsService.remove(Lists.newArrayList(id));
		// 保存自依赖信息
		if (BooleanUtils.isTrue(graphDto.getIsSelfDependent())) {
			JobDepends depends = new JobDepends();
			depends.setJobId(id);
			depends.setParentId(id);
			depends.setCreateUser(userDto.getLocalUserId());
			depends.setUpdateUser(userDto.getLocalUserId());
			depends.setType(DependTypeEnum.SELF);
			jobDependsService.save(depends);
		}
		
		// 保存其他节点依赖信息
		if (CollectionUtils.isNotEmpty(parents)) {
			List<JobDepends> dependses = parents.stream().map(graph -> {
				JobDepends depends = new JobDepends();
				depends.setJobId(id);
				depends.setParentId(graph.getId());
				depends.setType(graph.getType());
				depends.setCreateUser(userDto.getLocalUserId());
				depends.setUpdateUser(userDto.getLocalUserId());
				return depends;
			}).collect(Collectors.toList());
			jobDependsService.saveBatch(dependses);
		}
		jobService.needCheckCycle(id);
	}

	private void checkDepends(Long id, List<GraphDto> parents) {
		// 类型检查
		for (GraphDto parent : parents) {
			if (parent.getType() == null) {
				throw new BizException(ErrorCode.JOB_DEPEND_TYPE_NOT_EMPTY);
			}
		}
		// 任务是否存在检查
		List<Long> ids = parents.stream().map(GraphDto::getId).collect(Collectors.toList());
		ids.add(id);
		List<Job> jobs = jobService.listByIdsIfNotDelete(ids);
		if (CollectionUtils.isEmpty(jobs)) {
			throw new BizException(ErrorCode.JOB_NOT_EXISTE);
		}
		// 父子节点，依赖类型检查
		Map<Long, JobCycleTypeEnum> cycleMap = jobs.stream().collect(Collectors.toMap(Job::getId, Job::getCycleType));
		Map<Long, String> nameMap = jobs.stream().collect(Collectors.toMap(Job::getId, Job::getName));
		
		JobCycleTypeEnum typeEnum = cycleMap.get(id);
		Assert.notNull(typeEnum);
		for (GraphDto parent : parents) {
			Long parentId = parent.getId();
			JobCycleTypeEnum typeParentEnum = cycleMap.get(parentId);

			if (DependTypeEnum.PREV.equals(parent.getType()) && (!JobCycleTypeEnum.DAY.equals(typeEnum) || !JobCycleTypeEnum.DAY.equals(typeParentEnum))) {
				throw new BizException(ErrorCode.NOT_ALLOW_PREV_DEPEND);
			}
		  	if (!JobCycleTypeEnum.checkIsDepends(typeEnum, typeParentEnum)) {
			  	throw new BizException(ErrorCode.NOT_ALLOW_DEPENDS.setParams(nameMap.get(id), typeEnum.getDesc(), nameMap.get(parentId), typeParentEnum.getDesc()));
			}
		}

		// 存在循环依赖
		List<Long> parentIds = parents.stream().map(GraphDto::getId).collect(Collectors.toList());
		if (checkJobOnlineCycle(id, parentIds)) {
			throw new BizException(ErrorCode.JOB_CYCLE_ERROR);
		}
	}

	// 检出循环依赖
	private boolean checkJobOnlineCycle(Long id, List<Long> parentIds) {
		Map<Long, Set<Long>> jobOnlineMap = new HashMap<>();
		List<JobOnlineDependsVO> jobOnlineDependsVOS = jobOnlineDependsService.listNoSelf();
		for (JobOnlineDependsVO jobOnlineDependsVO : jobOnlineDependsVOS) {
			Long jobId = jobOnlineDependsVO.getJobId();
			Long parentId = jobOnlineDependsVO.getParentId();
			if (jobOnlineMap.containsKey(jobId)) {
				jobOnlineMap.get(jobId).add(parentId);
			} else {
				Set<Long> parentIdSet = new HashSet<>();
				parentIdSet.add(parentId);
				jobOnlineMap.put(jobId, parentIdSet);
			}
		}
		return new JobDependsCycleCheck(jobOnlineMap).checkCycle(id, parentIds);
	}

	public Vertex getJobGraph(GraphThendType type, Long id, Boolean isOnline) {
		log.info("type:{}, id:{},isOnline:{}", type, id, isOnline);
		if (BooleanUtils.isTrue(isOnline)) {
			return getJobOnlineGraph(type, id);
		} else {
			return getJobGraph(id);
		}
	}
	
	// 线下编辑只会取父节点，没有拿子节点的场景
	private Vertex getJobGraph(Long id) {
		Assert.notNull(id);
		
		Job job = jobService.getById(id);
		if (job == null) {
			return new Vertex();
		}
		// 当前节点
		List<JobDependsVO> dependsVOs = jobDependsService.listByJobId(id);
		List<JobDependsVO> dependsChildVOs = jobDependsService.listByParentId(id);
		
		Map<Long, String> targetNameMap = getTargetNameMap(id, dependsVOs);
		Vertex vertex = genJobOnlineVertex(job, dependsVOs, dependsChildVOs, targetNameMap);
		
		if (CollectionUtils.isEmpty(dependsVOs)) {
			return vertex;
		}
		// 取父节点信息
		List<Long> parentid = dependsVOs.stream().map(JobDependsVO::getParentId).collect(Collectors.toList());
		List<Job> jobs = jobService.listByIdsIfNotDelete(parentid);
		Map<Long, DependTypeEnum> dependTypeMap = dependsVOs.stream()
				.filter(depends -> DependTypeEnum.SELF != depends.getType())
				.collect(Collectors.toMap(JobDependsVO::getParentId, JobDependsVO::getType));
		for (Job vo : jobs) {
			// 自依赖，不放了
			if (id.equals(vo.getId())) {
				continue;
			}
			Vertex subVertex = new Vertex();
			subVertex.setId(vo.getId());
			subVertex.setName(vo.getName());
			subVertex.setTargetTable(targetNameMap.get(vo.getId()));
			subVertex.setScheduler(vo.getCycleType()
					.getSchedulerTime(JSON.parseObject(vo.getSchedulerTime(), SchedulerTimeDto.class)));

			Edge edge = new Edge();
			edge.setType(dependTypeMap.get(vo.getId()));
			edge.setVertex(subVertex);
			vertex.addParent(edge);
		}
		
		return vertex;
	}

	private Map<Long, String> getTargetNameMap(Long id, List<JobDependsVO> dependsVOs) {
		List<Long> ids = dependsVOs.stream().map(JobDependsVO::getParentId).collect(Collectors.toList());
		ids.add(id);
		List<Job> jobs = jobService.listByIdsIfNotDelete(ids);
		Map<Long, JobExtCommonDto> jobExtMap = jobBizService.getExtJobMap(jobs);
		if (MapUtils.isEmpty(jobExtMap)) {
			return Maps.newHashMap();
		}
		Map<Long, String> targetNameMap = Maps.newHashMap();
		for (Entry<Long, JobExtCommonDto> entries : jobExtMap.entrySet()) {
			targetNameMap.put(entries.getKey(), entries.getValue().getTargetTable());
		}
		return targetNameMap;
	}
	
	private Vertex getJobOnlineGraph(GraphThendType type, Long id) {
		Assert.notNull(id);
		
		JobOnline job = jobOnlineService.getById(id);
		if (job == null) {
			return new Vertex();
		}
		
		// 当前节点
		List<JobOnlineDependsVO> dependsVOs = jobOnlineDependsService.listByJobId(id);
		List<JobOnlineDependsVO> dependsChildVOs = jobOnlineDependsService.listByParentId(id);
		// 获取自依赖
		Vertex vertex = genJobOnlineVertex(job, dependsVOs, dependsChildVOs);
		if (type == null) {
			return vertex;
		}
		// 父节点展开
		if (GraphThendType.UP == type)  {
			if (CollectionUtils.isEmpty(dependsVOs) ) {
				return vertex;
			}
			// 取父节点信息
			List<Long> parentid = dependsVOs.stream().map(JobOnlineDependsVO::getParentId).collect(Collectors.toList());
			List<JobOnline> jobs = jobOnlineService.listByIdsIfNotDelete(Lists.newArrayList(parentid));
			Map<Long, DependTypeEnum> dependTypeMap = dependsVOs.stream().collect(Collectors.toMap(JobOnlineDependsVO::getParentId, JobOnlineDependsVO::getType));
			for (JobOnline jobOnline :  jobs) {
				// 自依赖，不放了
				if (id.equals(jobOnline.getJobId())) {
					continue;
				}
				Edge edge = genGraph(jobOnline, dependTypeMap);
				vertex.addParent(edge);
			}
		} else if (GraphThendType.DOWN == type)  {
			if (CollectionUtils.isEmpty(dependsChildVOs) ) {
				return vertex;
			}
			// 取子节点信息
			List<Long> childId = dependsChildVOs.stream().map(JobOnlineDependsVO::getJobId).collect(Collectors.toList());
			List<JobOnline> childJobs = jobOnlineService.listByIdsIfNotDelete(Lists.newArrayList(childId));
			Map<Long, DependTypeEnum> dependTypeMap = dependsChildVOs.stream().collect(Collectors.toMap(JobOnlineDependsVO::getJobId, JobOnlineDependsVO::getType));
			for (JobOnline childJobOnline :  childJobs) {
				// 自依赖，不放了
				if (id.equals(childJobOnline.getJobId())) {
					continue;
				}
				Edge edge = genGraph(childJobOnline, dependTypeMap);
				vertex.addChild(edge);
			}
		}
		return vertex;
	}
	
	private Vertex genJobOnlineVertex(Job job, List<JobDependsVO> dependsVOs,
			List<JobDependsVO> dependsChildVOs,Map<Long, String> targetNameMap ) {
		Vertex vertex = new Vertex();
		Long id = job.getId();
		vertex.setId(id);
		vertex.setName(job.getName());
		vertex.setIsSelfDependent(false);
		vertex.setTargetTable(targetNameMap.get(id));
		vertex.setScheduler(job.getCycleType().getSchedulerTime(JSON.parseObject(job.getSchedulerTime(), SchedulerTimeDto.class)));
		// 寻找自依赖
		for (JobDependsVO dependsVO :  dependsVOs) {
			if (dependsVO.getParentId().equals(id)) {
				vertex.setIsSelfDependent(true);
			}
		}
		for (JobDependsVO dependsVO :  dependsChildVOs) {
			if (dependsVO.getJobId().equals(id)) {
				vertex.setIsSelfDependent(true);
			}
		}
		return vertex;
	}

	public Vertex genJobOnlineVertex(JobOnline job, List<JobOnlineDependsVO> dependsVOs,
			List<JobOnlineDependsVO> dependsChildVOs) {
		Vertex vertex = new Vertex();
		Long id = job.getJobId();
		vertex.setId(id);
		vertex.setName(job.getName());
		vertex.setIsSelfDependent(false);
		vertex.setTargetTable(job.getTargetTable());
		vertex.setScheduler(job.getCycleType().getSchedulerTime(JSON.parseObject(job.getSchedulerTime(), SchedulerTimeDto.class)));
		// 寻找自依赖
		for (JobOnlineDependsVO dependsVO :  dependsVOs) {
			if (dependsVO.getParentId().equals(id)) {
				vertex.setIsSelfDependent(true);
			}
		}
		for (JobOnlineDependsVO dependsVO :  dependsChildVOs) {
			if (dependsVO.getJobId().equals(id)) {
				vertex.setIsSelfDependent(true);
			}
		}
		return vertex;
	}

	public Edge genGraph(JobOnline childJob, Map<Long, DependTypeEnum> dependTypeMap) {
		Vertex subVertex = new Vertex();
		subVertex.setId(childJob.getJobId());
		subVertex.setName(childJob.getName());
		subVertex.setTargetTable(childJob.getTargetTable());
		subVertex.setScheduler(childJob.getCycleType().getSchedulerTime(JSON.parseObject(childJob.getSchedulerTime(), SchedulerTimeDto.class)));
			
		Edge edge = new Edge();
		edge.setType(dependTypeMap.get(childJob.getJobId()));
		edge.setVertex(subVertex);
		return edge;
	}
	
	
	private void genChildVertex(Long id, Vertex vertex) {
		List<TaskDependsVO> dependsVOs = taskDependsService.listParentByTaskId(id);
		if (CollectionUtils.isEmpty(dependsVOs) ) {
			return;
		}
		List<Long> childIds = dependsVOs.stream().map(TaskDependsVO::getTaskId).collect(Collectors.toList());
		List<TaskVO> vos = listTasks(childIds);
		List<Edge> edges = genEdges(vos);
		vertex.setChilds(edges);
	}

	private void genParentVertex(Long id, Vertex vertex) {
		List<TaskDependsVO> dependsVOs = taskDependsService.listByTaskId(id);
		if (CollectionUtils.isEmpty(dependsVOs) ) {
			return;
		}
		
		List<Long> parentIds = dependsVOs.stream().map(TaskDependsVO::getParentId).collect(Collectors.toList());
		
		List<TaskVO> vos = listTasks(parentIds);
		List<Edge> edges = genEdges(vos);
		vertex.setParents(edges);
	}
	
	private void genAllParentVertex(Long id, Vertex vertex) {
		List<TaskDependsVO> dependsVOs = taskDependsService.listByTaskId(id);
		if (CollectionUtils.isEmpty(dependsVOs) ) {
			return;
		}
		
		List<Long> parentIds = dependsVOs.stream().map(TaskDependsVO::getParentId).collect(Collectors.toList());
		
		List<TaskDependsVO> parentTaskDependsVOs = taskDependsService.listByTaskIds(parentIds);
		parentIds.addAll(parentTaskDependsVOs.stream().map(TaskDependsVO::getParentId).collect(Collectors.toList()));
		
		List<TaskVO> vos = listTasks(parentIds);
		List<Edge> edges = genEdges(vos);
		vertex.setParents(edges);
	}	
	
	public Vertex getTaskGraph(Long id, Vertex vertex) {
		// 查询父节点
		genParentVertex(id, vertex);

		// 查询子节点
		genChildVertex(id, vertex);
		
		return vertex;
	}
	
	public Vertex getTaskGraph(GraphThendType type, Long id) {
		Assert.notNull(id);
		
		TaskVO task = taskService.getById(id);
		if (task == null) {
			return new Vertex();
		}
		Vertex vertex = this.genCurrentVertex(task);
		
		// 查询当前节点
		if (type == null) {
			return this.getTaskGraph(id, vertex);
		}
		
		// 父节点展开
		if (GraphThendType.UP == type)  {
			genParentVertex(id, vertex);
		} else if (GraphThendType.DOWN == type)  {
			genChildVertex(id, vertex);
		}
		
		return vertex;
	}

	private List<TaskVO> listTasks(List<Long> ids) {
		List<TaskVO> vos = taskService.listNormalByIds(ids);
		Map<Long, String> targetNameMap = jobOnlineService.getTargetMap(vos.stream().map(Task::getJobId).collect(Collectors.toList()));
		vos.stream().forEach(vo -> {
			vo.setTargetName(targetNameMap.get(vo.getJobId()));
		});
		return vos;
	}

	private Vertex genCurrentVertex(TaskVO task) {
		Map<Long, String> taskTargetNameMap = jobOnlineService.getTargetMap(Lists.newArrayList(task.getJobId()));
		task.setTargetName(taskTargetNameMap.get(task.getJobId()));
		return generVertex(task);
	}

	private List<Edge> genEdges(List<TaskVO> tasks) {
		List<Edge> edges = Lists.newArrayList();
		for (TaskVO task : tasks) {
			Vertex subVertex = generVertex(task);
				
			Edge edge = new Edge();
			edge.setVertex(subVertex);
			edges.add(edge);
		}
		return edges;
	}

	private Vertex generVertex(TaskVO task) {
		Vertex subVertex = new Vertex();
		subVertex.setName(task.getName());
		subVertex.setId(task.getId());
		subVertex.setStatus(task.getStatus());
		subVertex.setStartTime(task.getStartTime());
		subVertex.setTaskTime(task.getTaskTime());
		subVertex.setExecTime(task.getExecTime());
		subVertex.setEndTime(task.getEndTime());
		subVertex.setTargetTable(task.getTargetName());
		if (task.getEndTime() != null) {
			subVertex.setDur(DateUtil.getDatePoor(task.getStartTime(),task.getEndTime(), 1000));
		}
		return subVertex;
	}
	
	public void checkCycle() {
		List<JobVO> jobVos = jobService.listCheckCycle();
		List<Long> passIds = Lists.newArrayList();
		List<Long> noPassIds = Lists.newArrayList();
		for (JobVO job : jobVos) {
			Boolean isPass = this.isCycleCheck(job.getId());
			if (isPass) {
				passIds.add(job.getId());
			} else {
				noPassIds.add(job.getId());
			}
		}
		if (CollectionUtils.isNotEmpty(passIds)) {
			Job job = new Job();
			job.setIds(passIds);
			Map<String, Object> updateParam = Maps.newHashMap();
			updateParam.put("is_check_cycle",false);
			job.setUpdateParam(updateParam);
			jobService.edit(job);
		}
		if (CollectionUtils.isNotEmpty(noPassIds)) {
			Job job = new Job();
			job.setIds(noPassIds);
			Map<String, Object> updateParam = Maps.newHashMap();
			updateParam.put("err_msg",ErrorCode.JOB_CYCLE_ERROR.getErrorMsg());
			updateParam.put("status", JobStatusEnum.FAILED);
			updateParam.put("is_check_cycle", false);
			job.setUpdateParam(updateParam);
			jobService.edit(job);
		}
	}
	
	// 检查循环依赖
	// 找到设置节点的，是否曾经设置过子依赖，有，从那次节点往下找是否包含当前节点
	private boolean isCycleCheck(Long jobId) {
		Assert.notNull(jobId);
		// 没有依赖信息
		List<JobDependsVO> dends = jobDependsService.listByJobId(jobId);
		if (CollectionUtils.isEmpty(dends)) {
			return true;
		}
		List<Long> ids = Lists.newArrayList(jobId);
		List<Long> parentIds = dends.stream().map(JobDependsVO::getParentId).collect(Collectors.toList());
		List<JobDependsVO> parentDends = Lists.newArrayList();
		while (true) {
			parentDends = jobDependsService.listByParentIds(ids);
			// 去除自依赖信息
			if (CollectionUtils.isNotEmpty(parentDends)) {
				Iterator<JobDependsVO> iterators = parentDends.iterator();
				while(iterators.hasNext()) {
					JobDependsVO jobDependsVO = iterators.next();
					if (jobDependsVO.getJobId().equals(jobDependsVO.getParentId())) {
						iterators.remove();
					}
				}
			}
			if (CollectionUtils.isEmpty(parentDends)) {
				return true;
			}
			// 查询的子节点包含上次的父节点，则不通过
			ids = parentDends.stream().map(JobDependsVO::getJobId).collect(Collectors.toList());
			for (Long id : parentIds) {
				if (ids.contains(id)) {
					return false;
				}
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
    /**
     * 获取依赖搜索的实例列表
     * 
     * @param taskId
     * @return
     */
    public List<Content> listTasksByDependId(Long taskId) {
    	Assert.notNull(taskId);
    	
    	Task task = taskService.getById(taskId);
    	if (BooleanUtils.isTrue(task.getIsTemp())) {
    		return Lists.newArrayList();
    	}
    	List<Task> tasks = taskService.listByJobId(task.getJobId());
    	return tasks.stream().sorted(Comparator.comparing(Task::getTaskTime).reversed()).map(dto -> {
    		Content content = new Content();
    		content.setDesc(DateUtil.format(dto.getTaskTime(), DateFormatPattern.YYYY_MM_DD_HH_MM_SS));
    		content.setValue(dto.getId());
    		return content;
    	}).limit(2_000).collect(Collectors.toList());
    }
	
}
