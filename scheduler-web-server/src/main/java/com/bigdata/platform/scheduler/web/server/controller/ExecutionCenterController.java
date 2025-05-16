package com.bigdata.platform.scheduler.web.server.controller;

import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.web.server.service.TaskBizService;
import com.bigdata.platform.scheduler.web.server.vo.TaskOverviewVo;
import com.bigdata.platform.scheduler.web.server.vo.TaskQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bigdata.platform.scheduler.common.bean.BaseResult;
import com.bigdata.platform.scheduler.common.bean.PageUtils;
import com.bigdata.platform.scheduler.web.server.dto.OverviewDto;
import com.bigdata.platform.scheduler.web.server.dto.TaskQueryDto;
import com.bigdata.platform.scheduler.web.server.login.LocalUser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author xiejiajun
 */
@Api(tags = "运行中心")
@RestController
@RequestMapping("/center")
public class ExecutionCenterController {

    @Autowired
    private TaskBizService taskBizService;

    @ApiOperation(value="运行总览", notes="根据统计维度获取运行信息")
    @ApiImplicitParam(name = "queryDto", value = "查询条件", dataType = "OverviewDto")
    @PostMapping("/overview/list")
    public BaseResult<PageUtils<TaskOverviewVo>> listByPage(@RequestBody OverviewDto queryDto) {
        PageUtils<TaskOverviewVo> pages = taskBizService.queryPage(queryDto);
        return new BaseResult<PageUtils<TaskOverviewVo>>(pages);
    }
    
    @ApiOperation(value="失败任务", notes="")
    @PostMapping("/listFailedTasksByPage")
    public BaseResult<PageUtils<TaskQueryVo>> listFailedTasksByPage(@RequestBody TaskQueryDto queryDto) {
    	LoginUserDto userDto = LocalUser.get();
    	PageUtils<TaskQueryVo> pageUtils = taskBizService.queryFailedPage(queryDto, userDto);
    	return new BaseResult<PageUtils<TaskQueryVo>>(pageUtils);
    }
    
    @ApiOperation(value="耗时任务", notes="")
    @PostMapping("/listDelayTasksByPage")
    public BaseResult<PageUtils<TaskQueryVo>> listDelayTasksByPage(@RequestBody TaskQueryDto queryDto) {
    	LoginUserDto userDto = LocalUser.get();
    	PageUtils<TaskQueryVo> pageUtils = taskBizService.queryDelayPage(queryDto, userDto);
    	return new BaseResult<PageUtils<TaskQueryVo>>(pageUtils);
    }
}
