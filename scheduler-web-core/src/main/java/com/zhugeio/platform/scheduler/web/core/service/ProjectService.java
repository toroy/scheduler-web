package com.zhugeio.platform.scheduler.web.core.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.dao.ProjectMapper;
import com.zhugeio.platform.scheduler.dal.po.Project;
import com.google.common.collect.Maps;

@Service
public class ProjectService extends BaseNewService<Project, Project> {

    @Resource
    ProjectMapper projectMapper;

    @PostConstruct
    public void init() {
        setBaseMapper(projectMapper);
    }

    public List<String> getProjectNames() {
        return projectMapper.getProjectNames();

    }
    
    public Map<Long, String> getNameMap() {
    	Project project = new Project();
    	project.setIsDeleted(false);
    	List<Project> projects = this.list(project);
    	return projects.stream().collect(Collectors.toMap(Project::getId, Project::getProjectName, (oldValue, newValue) -> newValue));
    }
    
    public String getName(Long id) {
    	Assert.notNull(id);
    	Project project = new Project();
    	project.setIsDeleted(false);
    	project.setId(id);
    	Project projectVO = this.get(project);
    	if (projectVO == null) {
    		return null;
    	}
    	return projectVO.getProjectName();
    }

	public List<Project> listByCreateUser(Long userId) {
		Assert.notNull(userId);
		Project project = new Project();
		project.setIsDeleted(false);
		project.setCreateUser(userId);
		return this.list(project);
	}
	
	public void editOwnerByIds(Long userId, Long targetUserId, List<Long> ids) {
		Assert.notNull(userId);
		Assert.notNull(targetUserId);
		Assert.collectionNotEmpty(ids, "id列表");
		
		Project project = new Project();
		project.setIsDeleted(false);
		project.setCreateUser(userId);
		project.setIds(ids);
		Map<String, Object> updateParam = Maps.newHashMap();
		project.setUpdateParam(updateParam);
		updateParam.put("create_user", targetUserId);
		this.edit(project);
	}
}
