package com.clubfactory.platform.scheduler.web.server.service;

import com.clubfactory.platform.scheduler.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.po.Project;
import com.clubfactory.platform.scheduler.web.core.service.ProjectService;
import com.clubfactory.platform.scheduler.web.core.service.UserService;
import com.clubfactory.platform.scheduler.web.core.vo.ProjectVO;
import com.clubfactory.platform.scheduler.web.server.dto.ProjectDto;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import lombok.extern.slf4j.Slf4j;

import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ProjectBizService {
    @Resource
    private ProjectService projectService;
    @Resource
    private UserService userService;

    public boolean addProject(ProjectDto projectDto) {
        String projectName = projectDto.getProjectName().trim();
        if (!checkExsit(projectName)) {
            Project project = new Project();
            BeanUtils.copyProperties(projectDto, project);
            project.setProjectName(projectName);
            LoginUserDto userDto = LocalUser.get();
            project.setCreateUser(userDto.getLocalUserId());
            project.setUpdateUser(userDto.getLocalUserId());

            Project result = projectService.save(project);
            return null == result ? false : true;
        } else {
            throw new BizException("duplicate project name:" + projectName);
        }
    }

    public boolean updateProject(ProjectDto projectDto, LoginUserDto userDto) {
        Assert.notNull(projectDto.getProjectId(), "projectId ");
        
        Project project = new Project();
        String projectName = projectDto.getProjectName().trim();
        project.setId(projectDto.getProjectId());
        project = projectService.get(project);
        if (!checkExsit(projectName) || projectName.equals(project.getProjectName())) {
        	
            project = new Project();
            project.setId(projectDto.getProjectId());
            Map<String, Object> updateParam = Maps.newHashMap();
            project.setUpdateParam(updateParam);
            updateParam.put("description", projectDto.getDescription());
            updateParam.put("project_name", projectName);
            updateParam.put("update_user", userDto.getLocalUserId());
            Project result = projectService.edit(project);
            return null == result ? false : true;
        } else {
            throw new BizException("duplicate project name:" + projectName);
        }
    }

    private Boolean checkExsit(String projectName) {
        List<String> projectNames = getProjectNames();
        Optional<String> o = Optional.ofNullable(projectNames).get().stream().filter(item -> item.equals(projectName)).findFirst();
        if (o.isPresent()) {
            return true;
        }
        return false;
    }

    public int deleteProject(Long projectId) {
        Project project = new Project();
        project.setId(projectId);
        project.setUpdateUser(LocalUser.get().getLocalUserId());
        return projectService.logicRemove(project);
    }

    public PageUtils<ProjectVO> listByPage(String searchKey, Integer pageSize, Integer pageNo) {
        Project project = new Project();
        project.setPageNo(pageNo);
        project.setPageSize(pageSize);
        project.setProjectName(searchKey);
        project.setIsDeleted(false);
        project.setCreateUser(LocalUser.get().getLocalUserId());
        PageUtils<Project> projects = projectService.pageList(project);
        List<Project> projectList = projects.getRows();
        List<ProjectVO> projectVOList = new ArrayList<>(projectList.size());
        projectList.forEach(item -> {
            ProjectVO projectVO = ProjectVO.builder().createUserStr(userService.getUserName(item.getCreateUser())).updateUserStr(userService.getUserName(item.getCreateUser())).build();
            BeanUtils.copyProperties(item, projectVO);
            projectVO.setProjectId(item.getId());
            projectVOList.add(projectVO);
        });

        return new PageUtils(projectVOList, projects.getTotalCount(), projects.getPageSize(), projects.getPageNo());
    }

    public List<String> getProjectNames() {
        List<String> projectNames = projectService.getProjectNames();
        Optional.ofNullable(projectNames).get().sort(String.CASE_INSENSITIVE_ORDER);
        return projectNames;
    }
}
