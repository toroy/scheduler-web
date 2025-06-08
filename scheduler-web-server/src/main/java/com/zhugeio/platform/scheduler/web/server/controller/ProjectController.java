package com.zhugeio.platform.scheduler.web.server.controller;

import java.util.List;

import javax.annotation.Resource;

import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.AssistantBizService;
import com.zhugeio.platform.scheduler.web.server.service.ProjectBizService;
import com.zhugeio.platform.scheduler.web.server.vo.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.dal.po.Project;
import com.zhugeio.platform.scheduler.web.core.enums.ErrorCode;
import com.zhugeio.platform.scheduler.web.core.vo.ProjectVO;
import com.zhugeio.platform.scheduler.web.server.dto.ProjectDto;
import com.zhugeio.platform.scheduler.web.server.dto.ProjectPagerDto;
import com.zhugeio.platform.scheduler.web.server.login.LocalUser;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/project")
@Slf4j
public class ProjectController {
    @Resource
    private ProjectBizService projectBizService;
    @Autowired
    private AssistantBizService assistantBizService;

    @PostMapping("add")
    public BaseResult<Boolean> addProject(@RequestBody @Validated ProjectDto projectDto) {
        boolean result = projectBizService.addProject(projectDto);
        log.info("{} result:{}", "add project", JSON.toJSONString(result));
        return true == result ? new BaseResult<Boolean>(result) : new BaseResult<Boolean>(ErrorCode.HANDLE_PROJECT_ERROR.setParams("ADD"));

    }

    @PostMapping("update")
    public BaseResult<Boolean> updateProject(@RequestBody @Validated ProjectDto projectDto) {
        LoginUserDto userDto = LocalUser.get();
        boolean result = projectBizService.updateProject(projectDto, userDto);
        return true == result ? new BaseResult<Boolean>(result) : new BaseResult<Boolean>(ErrorCode.HANDLE_PROJECT_ERROR.setParams("UPDATE"));
    }

    @GetMapping("listByPage")
    public BaseResult<PageUtils<ProjectVO>> listByPage(@RequestParam String searchKey, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize, @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo) {
        PageUtils<ProjectVO> result = projectBizService.listByPage(searchKey.trim(), pageSize, pageNo);
        log.info("{} result:{}", "listByPage of Project", JSON.toJSONString(result));
        return new BaseResult<>(result);
    }

    @GetMapping("delete/{projectId}")
    public BaseResult<Boolean> deleteProject(@PathVariable Long projectId) {
        int result = projectBizService.deleteProject(projectId);
        log.info("{} result:{}", "delete project", JSON.toJSONString(result));
        return 0 < result ? new BaseResult<Boolean>(true) : new BaseResult<Boolean>(ErrorCode.HANDLE_PROJECT_ERROR.setParams("DELETE"));

    }

    @GetMapping("getProjectNames")
    public BaseResult<List<String>> getProjectNames() {
        return new BaseResult<>(projectBizService.getProjectNames());
    }


    @ApiOperation(value="任务依赖视图", notes="任务依赖视图")
    @GetMapping("graph")
    public BaseResult<Vertex> graph(Long id) {
        Vertex vertex = assistantBizService.getProjectGraph(id);
        return new BaseResult<Vertex>(vertex);
    }


    @ApiOperation(value="列表查询", notes="列表查询")
    @PostMapping("/search/list")
    public BaseResult<PageUtils<Project>> listProjects(@RequestBody ProjectPagerDto projectPagerDto){
        return new BaseResult<PageUtils<Project>>(assistantBizService.queryProjectByPage(projectPagerDto));
    }

}
