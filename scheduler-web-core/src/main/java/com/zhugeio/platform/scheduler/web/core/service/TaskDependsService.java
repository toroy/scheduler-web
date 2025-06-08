package com.zhugeio.platform.scheduler.web.core.service;

import com.zhugeio.platform.scheduler.common.constant.DateFormatPattern;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.common.util.DateUtil;
import com.zhugeio.platform.scheduler.dal.dao.TaskDependsMapper;
import com.zhugeio.platform.scheduler.dal.po.TaskDepends;
import com.zhugeio.platform.scheduler.web.core.vo.TaskDependsVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class TaskDependsService extends BaseNewService<TaskDependsVO,TaskDepends> {

    @Resource
    TaskDependsMapper taskDependsMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(taskDependsMapper);
    }
    
    public List<TaskDependsVO> listByCreateDate(Date date) {
    	Assert.notNull(date);
    	
    	TaskDepends depends = new TaskDepends();
    	depends.setStartDate(DateUtil.format(date, DateFormatPattern.YYYY_MM_DD_HH_MM_SS));
    	return this.list(depends);
    }

    public  List<Long> listIdsByParent(Long parentId) {
    	return this.listIdsByParentIds(Lists.newArrayList(parentId));
	}

	public Map<Long, List<Long>> getParentMap(List<Long> parentIds) {
		Assert.collectionNotEmpty(parentIds, "ids");
		TaskDepends taskDepends = new TaskDepends();
		taskDepends.setIds(parentIds);
		taskDepends.setIsDeleted(false);
		taskDepends.setQueryListFieldName("parent_id");
		List<TaskDependsVO> vos = this.list(taskDepends);
		if (CollectionUtils.isEmpty(vos)) {
			return Maps.newHashMap();
		}
		return vos.stream().collect(groupingBy(TaskDependsVO::getParentId, HashMap::new, Collectors.mapping(TaskDependsVO::getId, toList())));
	}

    public List<Long> listIdsByParentIds(List<Long> ids) {
    	Assert.collectionNotEmpty(ids, "ids");
    	TaskDepends taskDepends = new TaskDepends();
    	taskDepends.setIds(ids);
    	taskDepends.setIsDeleted(false);
    	taskDepends.setQueryListFieldName("parent_id");
    	List<TaskDependsVO> vos = this.list(taskDepends);
    	if (CollectionUtils.isEmpty(vos)) {
    		return Lists.newArrayList();
    	}
    	return vos.stream().map(TaskDepends::getTaskId).collect(toList());
    }
    
	public List<TaskDependsVO> listByTaskId(Long taskId) {
		Assert.notNull(taskId);
		
		TaskDepends depend = new TaskDepends();
		depend.setTaskId(taskId);
		depend.setIsDeleted(false);
		return this.list(depend);
	}
	
	public List<TaskDependsVO> listByTaskIds(List<Long> taskIds) {
		Assert.collectionNotEmpty(taskIds, "ids");
		
		TaskDepends depend = new TaskDepends();
		depend.setIds(taskIds);
		depend.setQueryListFieldName("task_id");
		depend.setIsDeleted(false);
		return this.list(depend);
	}
	
	public List<TaskDependsVO> listParentByTaskId(Long taskId) {
		Assert.notNull(taskId);
		
		TaskDepends depend = new TaskDepends();
		depend.setParentId(taskId);
		depend.setIsDeleted(false);
		return this.list(depend);
	}

	public Boolean removeByParentIds(List<Long> ids) {
    	if (CollectionUtils.isEmpty(ids)) {
    		return true;
		}
		TaskDepends depend = new TaskDepends();
		depend.setIsDeleted(false);
		depend.setIds(ids);
		depend.setQueryListFieldName("parent_id");
		this.remove(depend);
		return true;
	}

	public Boolean removeByTaskIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return true;
		}
		TaskDepends depend = new TaskDepends();
		depend.setIsDeleted(false);
		depend.setIds(ids);
		depend.setQueryListFieldName("task_id");
		this.remove(depend);
		return true;
	}
}
