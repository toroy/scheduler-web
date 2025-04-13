package com.clubfactory.platform.scheduler.web.server.controller;

import com.clubfactory.platform.scheduler.common.bean.BaseResult;
import com.clubfactory.platform.scheduler.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.dal.po.Task;
import com.clubfactory.platform.scheduler.logger.vo.LogVO;
import com.clubfactory.platform.scheduler.web.core.enums.GraphThendType;
import com.clubfactory.platform.scheduler.web.server.dto.LastNRowsLogDto;
import com.clubfactory.platform.scheduler.web.server.dto.LoggerDto;
import com.clubfactory.platform.scheduler.web.server.dto.TaskQueryDto;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.service.GraphBizService;
import com.clubfactory.platform.scheduler.web.server.service.LoggerService;
import com.clubfactory.platform.scheduler.web.server.service.TaskBizService;
import com.clubfactory.platform.scheduler.web.server.vo.JobEnumVo.Content;
import com.clubfactory.platform.scheduler.web.server.vo.LogMapVo;
import com.clubfactory.platform.scheduler.web.server.vo.TaskQueryVo;
import com.clubfactory.platform.scheduler.web.server.vo.Vertex;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "实例中心")
@RestController
@RequestMapping("/task")
public class TaskController<T> {

	@Resource
	TaskBizService taskBizService;
	@Resource
	GraphBizService graphBizService;

	@Autowired
	private LoggerService loggerService;


	@ApiOperation(value="日志映射关系拉取", notes="日志映射关系拉取")
	@ApiImplicitParam(name = "taskId", value = "task实例Id", dataType = "Long")
	@GetMapping("/listLogMapInfos")
	public BaseResult<List<LogMapVo>> listLogMapInfos(Long taskId) {
		return new BaseResult<>(loggerService.listLogMapInfo(taskId));
	}

	@GetMapping("/getYarnAppId")
	public BaseResult<String> getYarnAppId(Long taskId) {
		return new BaseResult<>(taskBizService.getYarnAppId(taskId));
	}

	@ApiOperation(value="日志拉取", notes="日志拉取")
	@ApiImplicitParam(name = "loggerDto", value = "日志拉取参数", dataType = "LoggerDto")
	@PostMapping("/getLog")
	public BaseResult<LogVO> getLogs(@RequestBody LoggerDto loggerDto) {
		return new BaseResult<>(loggerService.queryLog(loggerDto));
	}

	@ApiOperation(value="拉取日志最后N行", notes="日志拉取")
	@ApiImplicitParam(name = "lastNRowsLogDto", value = "最后N行日志拉取参数", dataType = "LastNRowsLogDto")
	@PostMapping("/getLogLastNRows")
	public BaseResult<LogVO> listLogLastNRows(@RequestBody LastNRowsLogDto lastNRowsLogDto) {
		return new BaseResult<>(loggerService.queryLastNRowsLog(lastNRowsLogDto));
	}


	@ApiOperation(value="实例列表管理", notes="实例列表管理")
	@PostMapping("listByPage")
	public BaseResult<PageUtils<TaskQueryVo>> listByPage(@RequestBody TaskQueryDto queryDto) {
		LoginUserDto userDto = LocalUser.get();
		PageUtils<TaskQueryVo> pages = taskBizService.queryPage(queryDto, userDto);
		return new BaseResult<PageUtils<TaskQueryVo>>(pages);
	}

	@ApiOperation(value="重跑实例", notes="重跑实例")
	@PostMapping("rerunByIds")
	public BaseResult<Boolean> rerunByIds(@RequestBody Task task) {
		LoginUserDto userDto = LocalUser.get();
		taskBizService.reRun(task.getIds(), userDto);
		return new BaseResult<Boolean>(true);
	}

	@ApiOperation(value="重跑本实例及子实例", notes="重跑本实例及子实例")
	@PostMapping("rerunAllByIds")
	public BaseResult<Boolean> rerunAllByIds(@RequestBody Task task) {
		LoginUserDto userDto = LocalUser.get();
		taskBizService.reRunAll(task.getIds(), userDto);
		return new BaseResult<Boolean>(true);
	}

	@ApiOperation(value="终止实例", notes="终止实例")
	@PostMapping("kill")
	public BaseResult<Boolean> kill(@RequestBody Task task) {
		LoginUserDto userDto = LocalUser.get();
		taskBizService.kill(task.getIds(), userDto);
		return new BaseResult<Boolean>(true);
	}

	@ApiOperation(value="强制成功", notes="强制成功")
	@PostMapping("editSuccess")
	public BaseResult<Boolean> editSuccess(@RequestBody Task task) {
		LoginUserDto userDto = LocalUser.get();
		taskBizService.editSuccess(task.getIds(), userDto);
		return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="恢复调度", notes="恢复调度")
	@PostMapping("regain")
	public BaseResult<Boolean> regain(@RequestBody Task task) {
		LoginUserDto userDto = LocalUser.get();
		taskBizService.regain(task.getIds(), userDto);
		return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="实例依赖视图", notes="实例依赖视图")
	@GetMapping("graph")
	public BaseResult<Vertex> graph(Long id, GraphThendType type) {
		Vertex vertex = graphBizService.getTaskGraph(type, id);
		return new BaseResult<Vertex>(vertex);
	}
	
	@ApiOperation(value="实例依赖搜索", notes="实例依赖搜索")
	@GetMapping("listTasksByDependId")
	public BaseResult<Map<String, Object>> listTasksByDependId(Long taskId) {
		List<Content> contents = graphBizService.listTasksByDependId(taskId);
		Map<String, Object> map = Maps.newHashMap();
		map.put("tasks", contents);
		return new BaseResult<Map<String, Object>>(map);
	}
}
