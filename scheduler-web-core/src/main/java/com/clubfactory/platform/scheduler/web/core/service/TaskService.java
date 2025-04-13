package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.scheduler.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.common.constant.DateFormatPattern;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.common.util.DateUtil;
import com.clubfactory.platform.scheduler.dal.dao.TaskMapper;
import com.clubfactory.platform.scheduler.dal.enums.JobTypeEnum;
import com.clubfactory.platform.scheduler.dal.enums.TaskStatusEnum;
import com.clubfactory.platform.scheduler.dal.po.Task;
import com.clubfactory.platform.scheduler.web.core.vo.TaskVO;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskService extends BaseNewService<TaskVO,Task> {
	
    @Resource
    TaskMapper taskMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(taskMapper);
    }
    
    public void editRetryByJobId(Long jobId, Integer retryMax, Integer retryDur) {
    	Assert.notNull(jobId);
    	Task task = new Task();
    	task.setJobId(jobId);
    	task.setIsDeleted(false);
    	task.setStatus(TaskStatusEnum.INIT);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("retry_max", retryMax);
    	updateParam.put("retry_dur", retryDur);
    	task.setUpdateParam(updateParam);
    	this.edit(task);
    }
    
    public void editScriptId(Long id, Long scriptId, Long userId, Boolean isAdmin) {
    	Assert.notNull(id);
    	Assert.notNull(scriptId);
    	Task task = new Task();
    	task.setId(id);
    	task.setIsDeleted(false);
    	if (BooleanUtils.isFalse(isAdmin)) {
    		task.setCreateUser(userId);
    	}
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("script_id", scriptId);
    	task.setUpdateParam(updateParam);
    	this.edit(task);
    	
    }
    
    public List<Task> listOnlineTaskByDate(String startTime, String endTime, Long jobId) {
    	Assert.notBlank(startTime, "开始时间");
    	Assert.notBlank(endTime, "结束时间");
    	Assert.notNull(jobId, "任务id");
    	
    	Task task = new Task();
    	task.setJobId(jobId);
    	task.setStartDate(startTime);
    	task.setEndDate(endTime);
    	task.setIsTemp(false);
    	// 预发测试一把，其他的操作实例的地方，是否过滤过
		task.setIsDeleted(false);
    	return taskMapper.listByTaskTime(task);
    }
    
    public List<TaskVO> listByIds(List<Long> ids, Long userId, Boolean isAdmin) {
    	Assert.collectionNotEmpty(ids, "ids");
    	
    	Task task = new Task();
    	task.setIds(ids);
    	task.setIsDeleted(false);
    	if (BooleanUtils.isFalse(isAdmin)) {
    		task.setCreateUser(userId);
    	}
    	return this.list(task);
    }

    public List<TaskVO> listDqcByIds(List<Long> ids) {
    	return this.listByIds(ids).stream().filter(taskVO -> taskVO.getJobType() == JobTypeEnum.DQC).collect(Collectors.toList());
	}

	public List<TaskVO> listNormalByIds(List<Long> ids) {
		return this.listByIds(ids).stream().filter(taskVO -> taskVO.getJobType() == JobTypeEnum.NORMAL).collect(Collectors.toList());
	}
    
    public List<TaskVO> listByIds(List<Long> ids) {
    	Assert.collectionNotEmpty(ids, "ids");
    	
    	Task task = new Task();
    	task.setIds(ids);
    	task.setIsDeleted(false);
    	task.setIsTemp(false);
    	return this.list(task);
    }
    
    public List<TaskVO> listAllByIds(List<Long> ids) {
    	Assert.collectionNotEmpty(ids, "ids");
    	
    	Task task = new Task();
    	task.setIds(ids);
    	task.setIsDeleted(false);
    	return this.list(task);
    }
    
    public List<TaskVO> listByJobId(Long jobId, Boolean isTemp) {
    	Assert.notNull(jobId);
    	
    	Task task = new Task();
    	task.setJobId(jobId);
    	task.setIsDeleted(false);
    	task.setIsTemp(isTemp);
    	return this.list(task);
    }
    
    public List<Task> listByJobId(Long jobId) {
    	Assert.notNull(jobId);
    	
    	Task task = new Task();
    	task.setJobId(jobId);
    	task.setIsDeleted(false);
    	task.setIsTemp(false);
    	return taskMapper.list(task);
    }

	public List<Task> listInitByJobId(Long jobId) {
		Assert.notNull(jobId);

		Task task = new Task();
		task.setJobId(jobId);
		task.setIsDeleted(false);
		task.setStatus(TaskStatusEnum.INIT);
		task.setIsTemp(false);
		return taskMapper.list(task);
	}
    
    public List<Task> listByJobIds(List<Long> jobIds, Boolean isTemp) {
    	Assert.collectionNotEmpty(jobIds, "任务列表");
    	
    	Task task = new Task();
    	task.setIds(jobIds);
    	task.setQueryListFieldName("job_id");
    	task.setIsDeleted(false);
    	task.setIsTemp(isTemp);
    	return taskMapper.list(task);
    }
    
    public Map<Long, List<Task>> getMapByJobIds(List<Long> jobIds) {
    	 List<Task> tasks = this.listByJobIds(jobIds, false);
    	 if (CollectionUtils.isEmpty(tasks)) {
    		 return Maps.newHashMap();
    	 }
    	 return tasks.stream().collect(Collectors.groupingBy(Task::getJobId));
    }
    
    public TaskVO getById(Long id) {
    	Assert.notNull(id, "id");
    	Task task = new Task();
    	task.setId(id);
    	return this.get(task);
    }
    
    public List<Long> listIdsByJobId(List<Long> jobIds) {
    	List<TaskVO> tasks = listByJobIds(jobIds);
    	return tasks.stream().map(TaskVO::getId).collect(Collectors.toList());
    }

	private List<TaskVO> listByJobIds(List<Long> jobIds) {
		Assert.collectionNotEmpty(jobIds, "任务列表id");
    	
    	Task task = new Task();
    	task.setIds(jobIds);
    	task.setQueryListFieldName("job_id");
    	List<TaskVO> tasks = this.list(task);
    	if (CollectionUtils.isEmpty(tasks)) {
    		return Lists.newArrayList();
    	}
		return tasks;
	}
    
    public Boolean delByIds(List<Long> ids) {
    	Assert.collectionNotEmpty(ids, "task列表id");
    	
    	Task task = new Task();
    	task.setIds(ids);
    	this.logicRemove(task);
    	return true;
    }
    
    public Boolean delByJobId(Long jobId) {
    	Assert.notNull(jobId);
    	
    	Task task = new Task();
    	task.setJobId(jobId);
    	this.logicRemove(task);
    	return true;
    }

	public Boolean delByJobIds(List<Long> jobIds, Boolean isTemp) {
		if (CollectionUtils.isEmpty(jobIds)) {
			return false;
		}

		Task task = new Task();
		task.setQueryListFieldName("job_id");
		task.setIds(jobIds);
		task.setIsTemp(isTemp);
		this.logicRemove(task);
		return true;
	}
    
    public Boolean editByJobId(Long jobId, Task reqTask) {
    	Assert.notNull(jobId);
    	
    	Task task = new Task();
    	task.setJobId(jobId);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("script_id", reqTask.getScriptId());
    	updateParam.put("type", reqTask.getType());
    	updateParam.put("priority", reqTask.getPriority());
    	updateParam.put("machine_id", reqTask.getMachineId());
    	task.setUpdateParam(updateParam);
    	this.edit(task);
    	return true;
    }
    
    public Boolean editStartTimeByJobId(Long jobId, Date startTime, Date currentDate) {
    	Assert.notNull(jobId);
    	Assert.notNull(startTime);
    	Assert.notNull(currentDate);
    	
    	Task task = new Task();
    	task.setJobId(jobId);
    	task.setStatus(TaskStatusEnum.INIT);
    	task.setStartDate(DateUtil.format(currentDate, DateFormatPattern.YYYY_MM_DD_HH_MM_SS));
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("start_time", startTime);
    	task.setUpdateParam(updateParam);
    	this.edit(task);
    	return true;
    }
    
    public Boolean delInitByJobIds(List<Long> jobIds) {
    	Assert.collectionNotEmpty(jobIds, "jobid");
    	
    	Task task = new Task();
    	task.setStatus(TaskStatusEnum.INIT);
    	task.setIds(jobIds);
    	task.setQueryListFieldName("job_id");
    	this.logicRemove(task);
    	return true;
    }
    
    public Boolean editInitByIdsWithoutDate(List<Long> ids,Long userId) {
    	Assert.collectionNotEmpty(ids, "ids");
    	 // 更新
    	Task task = new Task();
    	task.setIds(ids);
    	task.setIsDeleted(false);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("status", TaskStatusEnum.INIT);
    	updateParam.put("is_notice", false);
    	updateParam.put("end_time", null);
    	updateParam.put("exec_time", null);
    	updateParam.put("update_user", userId);
    	task.setUpdateParam(updateParam);
    	this.editIfNull(task);
    	return true;
    }
    
    public Boolean editInitByIds(List<Long> ids,Long userId) {
    	Assert.collectionNotEmpty(ids, "ids");
    	 // 更新
    	Task task = new Task();
    	task.setIds(ids);
    	task.setIsDeleted(false);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("status", TaskStatusEnum.INIT);
    	updateParam.put("start_time", new Date());
    	updateParam.put("is_notice", false);
    	updateParam.put("end_time", null);
    	updateParam.put("exec_time", null);
    	updateParam.put("update_user", userId);
    	task.setUpdateParam(updateParam);
    	this.editIfNull(task);
    	return true;
    }
    
    public Boolean editStopByJobIds(List<Long> ids, Long userId) {
    	Assert.collectionNotEmpty(ids, "ids");
    	 // 更新
    	Task task = new Task();
    	task.setIds(ids);
    	task.setQueryListFieldName("job_id");
    	task.setStatus(TaskStatusEnum.INIT);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("status", TaskStatusEnum.PAUSE);
    	updateParam.put("update_user", userId);
    	task.setUpdateParam(updateParam);
    	this.edit(task);
    	return true;
    }
    
    public Boolean editSuccessByIds(List<Long> ids, TaskStatusEnum taskStatus, Long userId) {
    	Assert.collectionNotEmpty(ids, "ids");
    	Assert.notNull(taskStatus);
    	 // 更新
    	Task task = new Task();
    	task.setIds(ids);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("status", taskStatus);
    	updateParam.put("update_user", userId);
    	task.setUpdateParam(updateParam);
    	this.edit(task);
    	return true;
    }
    
    public Boolean killByIds(List<Long> ids) {
    	Assert.collectionNotEmpty(ids, "ids");
    	 // 更新
    	Task task = new Task();
    	task.setStatus(TaskStatusEnum.RUNNING);
    	task.setIds(ids);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("status", TaskStatusEnum.KILLING);
    	task.setUpdateParam(updateParam);
    	this.edit(task);
    	return true;
    }
    
    public Boolean regainByJobIds(List<Long> jobIds) {
    	Assert.collectionNotEmpty(jobIds, "ids");
    	 // 更新
    	Task task = new Task();
    	task.setIds(jobIds);
    	task.setIsDeleted(false);
    	task.setStatus(TaskStatusEnum.PAUSE);
    	task.setQueryListFieldName("job_id");
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("status", TaskStatusEnum.INIT);
    	updateParam.put("start_time", new Date());
    	task.setUpdateParam(updateParam);
    	this.edit(task);
    	return true;
    }
    
    public Boolean regainByIds(List<Long> ids) {
    	Assert.collectionNotEmpty(ids, "ids");
    	 // 更新
    	Task task = new Task();
    	task.setIds(ids);
    	task.setIsDeleted(false);
    	task.setStatus(TaskStatusEnum.PAUSE);
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("status", TaskStatusEnum.INIT);
    	task.setUpdateParam(updateParam);
    	this.edit(task);
    	return true;
    }

    public PageUtils<Task> pageDelayList(Task po) {
        if (null == po) {
            return new PageUtils(new ArrayList<Task>(),0, po.getPageSize(), po.getPageNo());
        }
        try {
            int totalCount = taskMapper.countByDelay(po);
            if (totalCount <= 0) {
                return new PageUtils(new ArrayList<Task>(), 0, po.getPageSize(), po.getPageNo());
            }
            po.setTotalCount(totalCount);//设置总记录数，获取总页数
            //vo.initPage();//分页保护，防止参数传入过大
            //mybatis插件，分页前要先调用下这个语句
            PageHelper.startPage(po.getPageNo(), po.getPageSize(), false);
            List<Task> list = taskMapper.listByDelay(po);
            return new PageUtils(list, po.getTotalCount(), po.getPageSize(), po.getPageNo());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("mybatis分页查询出错:" + ex);
        }
        return new PageUtils(new ArrayList<Task>(), 0, po.getPageSize(), po.getPageNo());
    }
    
    public int editIfNull(Task task) {
    	Assert.notNull(task);
    	
    	task.setUpdateTime(new Date());
    	return taskMapper.editIfNull(task);
    }

	public void editResumeByDate(Date date, Long jobId) {
		Assert.notNull(date);
		Assert.notNull(jobId);
		
		Task task = new Task();
		task.setJobId(jobId);
		task.setStatus(TaskStatusEnum.PAUSE);
		task.setIsDeleted(false);
		task.setIsTemp(false);
		task.setStartTime(date);
		Map<String, Object> updateParam = Maps.newHashMap();
		task.setUpdateParam(updateParam);
		updateParam.put("status", TaskStatusEnum.INIT);
		this.edit(task);
	}

	public void editInitOwnerByJobIds(Long userId, Long targetUserId, List<Long> jobIds) {
		Task task = new Task();
		task.setIds(jobIds);
		task.setQueryListFieldName("job_id");
		task.setStatus(TaskStatusEnum.INIT);
		task.setIsDeleted(false);
		task.setCreateUser(userId);
		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("create_user", targetUserId);
		task.setUpdateParam(updateParam);
		this.edit(task);
	}
}
