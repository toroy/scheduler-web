package com.clubfactory.platform.scheduler.web.server.controller;

import com.clubfactory.platform.common.bean.BaseResult;
import com.clubfactory.platform.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.dal.po.Job;
import com.clubfactory.platform.scheduler.web.server.dto.AddTaskDto;
import com.clubfactory.platform.scheduler.web.server.dto.JobQueryDto;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.service.JobBizService;
import com.clubfactory.platform.scheduler.web.server.service.JobDetailBizService;
import com.clubfactory.platform.scheduler.web.server.vo.JobQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "任务中心")
@RestController
@RequestMapping("/jobOnline")
public class JobOnlineController {
	
	@Resource
	JobBizService jobBizService;
	@Resource
	JobDetailBizService jobDetailBizService;

	@ApiOperation(value="任务列表", notes="任务列表")
	@PostMapping("listByPage")
    public BaseResult<PageUtils<JobQueryVo>> listByPage(@RequestBody JobQueryDto queryDto) {
		LoginUserDto userDto = LocalUser.get();
		PageUtils<JobQueryVo> pages = jobBizService.queryOnlinePage(queryDto, userDto);
    	return new BaseResult<PageUtils<JobQueryVo>>(pages);
    }
	
	@ApiOperation(value="获取任务详情", notes="获取任务详情")
	@GetMapping("getJob")
	public BaseResult<Object> getJob(Long id) {
		LoginUserDto userDto = LocalUser.get();
		Object object = jobDetailBizService.getOnline(id, userDto);
		return new BaseResult<Object>(object);
	}
	
	@ApiOperation(value="下线任务", notes="下线任务")
	@PostMapping("disable")
    public BaseResult<Boolean> disable(@RequestBody Job job) {
		LoginUserDto userDto = LocalUser.get();
		jobBizService.disable(job.getIds(), userDto);
    	return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="恢复任务", notes="恢复任务")
	@PostMapping("resume")
    public BaseResult<Boolean> resume(@RequestBody Job job) {
		LoginUserDto userDto = LocalUser.get();
		jobBizService.resume(job.getIds(), userDto);
    	return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="暂停任务", notes="暂停任务")
	@PostMapping("stop")
	BaseResult<Boolean> stop(@RequestBody Job job) {
		LoginUserDto userDto = LocalUser.get();
		jobBizService.pause(job.getIds(), userDto);
    	return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="补录", notes="补录")
	@PostMapping("addTask")
    public BaseResult<List<Long>> addTask(@RequestBody AddTaskDto task) {
		LoginUserDto userDto = LocalUser.get();
		List<Long> ids = jobBizService.addTask(task, userDto);
    	return new BaseResult<List<Long>>(ids);
	}
}
