package com.bigdata.platform.scheduler.web.server.controller;

import com.bigdata.platform.scheduler.web.server.constant.JsonKey;
import com.bigdata.platform.scheduler.web.server.dto.AddTaskDto;
import com.bigdata.platform.scheduler.web.server.dto.ChangeDto;
import com.bigdata.platform.scheduler.web.server.dto.ColumnQueryDto;
import com.bigdata.platform.scheduler.web.server.dto.JobQueryDto;
import com.bigdata.platform.scheduler.web.server.login.LocalUser;
import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.web.server.service.*;
import com.bigdata.platform.scheduler.web.server.vo.*;
import com.bigdata.platform.scheduler.common.bean.BaseResult;
import com.bigdata.platform.scheduler.common.bean.PageUtils;
import com.bigdata.platform.scheduler.dal.po.Job;
import com.bigdata.platform.scheduler.dal.po.Param;
import com.bigdata.platform.scheduler.web.core.dto.GraphDto;
import com.bigdata.platform.scheduler.web.core.dto.JobCalDto;
import com.bigdata.platform.scheduler.web.core.dto.JobCollectDto;
import com.bigdata.platform.scheduler.web.core.dto.JobReflueDto;
import com.bigdata.platform.scheduler.web.core.enums.GraphThendType;
import com.bigdata.platform.scheduler.web.core.enums.JobPageType;
import com.bigdata.platform.scheduler.web.server.service.*;
import com.bigdata.platform.scheduler.web.server.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "任务中心")
@RestController
@RequestMapping("/job")
public class JobController<T> {
	
	@Resource
	JobBizService jobBizService;
	@Resource
	JobDetailBizService jobDetailBizService;
	@Resource
	GraphBizService graphBizService;
	@Resource
	JobUpdateService jobUpdateService;
	@Resource
	JobCheckBizService jobCheckBizService;

	@ApiOperation(value="新增计算任务", notes="新增计算任务")
	@PostMapping("saveCal")
    public BaseResult<Long> saveCal(@RequestBody JobCalDto jobDto) {
		LoginUserDto userDto = LocalUser.get();
		Long jobId = jobDetailBizService.saveCal(jobDto, userDto);
    	return new BaseResult<Long>(jobId);
    }
	
	@ApiOperation(value="新增采集任务", notes="新增采集任务")
	@PostMapping("saveCollect")
    public BaseResult<Long> saveCollect(@RequestBody JobCollectDto jobDto) {
		LoginUserDto userDto = LocalUser.get();
		Long jobId = jobDetailBizService.saveCollect(jobDto, userDto);
    	return new BaseResult<Long>(jobId);
    }
	
	@ApiOperation(value="新增回流任务", notes="新增回流任务")
	@PostMapping("saveReflue")
    public BaseResult<Long> saveReflue(@RequestBody JobReflueDto jobDto) {
		LoginUserDto userDto = LocalUser.get();
		Long jobId = jobDetailBizService.saveReflue(jobDto, userDto);
    	return new BaseResult<Long>(jobId);
    }
	
	@ApiOperation(value="修改计算任务", notes="修改计算任务")
	@PostMapping("editCal")
    public BaseResult<Boolean> editCal(@RequestBody JobCalDto jobDto) {
		LoginUserDto userDto = LocalUser.get();
		jobDetailBizService.editCal(jobDto, userDto);
    	return new BaseResult<Boolean>(true);
    }
	
	@ApiOperation(value="修改采集任务", notes="修改采集任务")
	@PostMapping("editCollect")
    public BaseResult<Boolean> editCollect(@RequestBody JobCollectDto jobDto) {
		LoginUserDto userDto = LocalUser.get();
		jobDetailBizService.editCollect(jobDto, userDto);
    	return new BaseResult<Boolean>(true);
    }
	
	@ApiOperation(value="修改回流任务", notes="修改回流任务")
	@PostMapping("editReflue")
    public BaseResult<Boolean> editReflue(@RequestBody JobReflueDto jobDto) {
		LoginUserDto userDto = LocalUser.get();
		jobDetailBizService.editReflue(jobDto, userDto);
    	return new BaseResult<Boolean>(true);
    }
	
	@ApiOperation(value="获取任务详情", notes="获取任务详情")
	@GetMapping("getJob")
	public BaseResult<Object> getJob(Long id) {
		LoginUserDto userDto = LocalUser.get();
		Object object = jobDetailBizService.get(id, userDto);
		return new BaseResult<Object>(object);
	}
	
	@ApiOperation(value="任务列表", notes="任务列表")
	@PostMapping("listByPage")
    public BaseResult<PageUtils<JobQueryVo>> listByPage(@RequestBody JobQueryDto queryDto) {
		LoginUserDto userDto = LocalUser.get();
		PageUtils<JobQueryVo> pages = jobBizService.queryPage(queryDto, userDto);
    	return new BaseResult<PageUtils<JobQueryVo>>(pages);
    }
	
	@ApiOperation(value="任务审核列表", notes="任务审核列表")
	@PostMapping("listCheckByPage")
    public BaseResult<PageUtils<JobQueryVo>> listCheckByPage(@RequestBody JobQueryDto queryDto) {
		LoginUserDto userDto = LocalUser.get();
		PageUtils<JobQueryVo> pages = jobBizService.queryCheckByPage(queryDto, userDto);
    	return new BaseResult<PageUtils<JobQueryVo>>(pages);
    }
	
	
	@ApiOperation(value="开启任务", notes="开启任务")
	@PostMapping("enable")
    public BaseResult<Boolean> enable(@RequestBody Job job) {
		LoginUserDto userDto = LocalUser.get();
		jobBizService.enable(job.getIds(), userDto);
    	return new BaseResult<Boolean>(true);
	}
	
    @ApiOperation(value="枚举类型类别", notes="枚举类型类别")
	@GetMapping("listEnums")
    public BaseResult<JobEnumVo> listEnums(JobPageType type) {
		JobEnumVo jobEnumVo = jobBizService.getJobEnum(type);
    	return new BaseResult<JobEnumVo>(jobEnumVo);
    }
	
    @ApiOperation(value="任务依赖视图", notes="任务依赖视图")
	@GetMapping("graph")
    public BaseResult<Vertex> graph(Long id, GraphThendType type, Boolean isOnline) {
		Vertex vertex = graphBizService.getJobGraph(type, id, isOnline);
    	return new BaseResult<Vertex>(vertex);
	}
	
    @ApiOperation(value="配置字段映射", notes="配置字段映射")
	@GetMapping("listColumnsByTable")
    public BaseResult<ColumnsVo> listColumnsByTable(ColumnQueryDto columnDto) {
		ColumnsVo columnsVo = jobDetailBizService.listColumnsByTable(columnDto);
    	return new BaseResult<ColumnsVo>(columnsVo);
	}
	
	@ApiOperation(value="补录", notes="补录")
	@PostMapping("addTask")
    public BaseResult<Long> addTask(@RequestBody AddTaskDto task) {
		LoginUserDto userDto = LocalUser.get();
		List<Long> ids = jobBizService.addTask(task, userDto);
    	return new BaseResult<Long>(ids.get(0));
	}
    
	@ApiOperation(value="任务依赖视图编辑", notes="任务依赖视图编辑")
	@PostMapping("editGraph")
    public BaseResult<Boolean> editGraph(@RequestBody GraphDto graphDto) {
		LoginUserDto userDto = LocalUser.get();
		graphBizService.editGrpah(graphDto, userDto);
    	return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="复制任务", notes="复制任务")
	@PostMapping("copyJob")
	BaseResult<Boolean> copyJob(@RequestBody Job job) {
		LoginUserDto userDto = LocalUser.get();
		jobBizService.copyJob(job.getIds(), userDto);
    	return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="审核通过", notes="审核通过")
	@PostMapping("editSuccess")
	BaseResult<Boolean> editSuccess(@RequestBody List<Job> jobs) {
		LoginUserDto userDto = LocalUser.get();
		jobCheckBizService.editSuccess(jobs, userDto);
    	return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="删除", notes="删除")
	@PostMapping("delete")
	BaseResult<Boolean> delete(@RequestBody Job job) {
		LoginUserDto userDto = LocalUser.get();
		jobBizService.delete(job.getIds(), userDto);
    	return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="审核通过单个", notes="审核通过单个")
	@GetMapping("editSuccessByJobId")
	BaseResult<Boolean> editSuccessByJobId(Long jobId) {
		LoginUserDto userDto = LocalUser.get();
		Boolean isSuccess = jobCheckBizService.editSuccessByJobId(jobId, userDto);
    	return new BaseResult<Boolean>(isSuccess);
	}
	
	@ApiOperation(value="申请审核", notes="申请审核")
	@PostMapping("check")
	BaseResult<Boolean> check(@RequestBody Job job) {
		LoginUserDto userDto = LocalUser.get();
		jobCheckBizService.checkJobs(job.getIds(), userDto);
    	return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="依赖视图查询", notes="依赖视图查询")
	@GetMapping("listJobByKey")
    public BaseResult<Map<String, List<VertexBase>>> listJobByKey(String key) {
		List<VertexBase> vos = jobBizService.listJobByKey(key);
		Map<String, List<VertexBase>> maps = Maps.newHashMap();
		maps.put("rows", vos);
    	return new BaseResult<Map<String, List<VertexBase>>>(maps);
	}
	
	@ApiOperation(value="自定义参数名称列表", notes="自定义参数名称列表")
	@GetMapping("listParamNames")
    public BaseResult<Map<String, List<String>>> listParamNames(Param param) {
		List<String> names = jobBizService.listParamNames(param);
		Map<String, List<String>> maps = Maps.newHashMap();
		maps.put("names", names);
    	return new BaseResult<Map<String, List<String>>>(maps);
	}

	@ApiOperation(value="任务移交", notes="任务移交")
	@PostMapping("changeOwner")
    public BaseResult<Boolean> changeOwner(@RequestBody ChangeDto changeDto) {
		LoginUserDto userDto = LocalUser.get();
		jobBizService.changeJobOwner(changeDto, userDto);
    	return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="系统任务移交", notes="系统任务移交")
	@PostMapping("changeSysOwner")
    public BaseResult<Boolean> changeSysOwner(@RequestBody ChangeDto changeDto) {
		LoginUserDto userDto = LocalUser.get();
		jobBizService.changeOwner(changeDto, userDto);
    	return new BaseResult<Boolean>(true);
	}

	@ApiOperation(value="根据数据源id，将dsUrl更新到相关的 job execParam 里", notes="")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "dsIds", value = "数据源id，多个用英文逗号隔开", dataType = "String")
	})
	@GetMapping("syncDsUrlToJobExecParam")
	public BaseResult<Boolean> syncDsUrlToJobExecParam(String dsIds) {
		jobUpdateService.updateJobxxTableAndExecParam(
				null, null, JsonKey.DS_URL, dsIds);
		return new BaseResult<Boolean>(true);
	}
	
	@ApiOperation(value="将dsUrl更新到相关的 job execParam 里", notes="")
	@GetMapping("syncDsJobExecParam")
	public BaseResult<Boolean> syncDsJobExecParam() {
		jobUpdateService.syncJobExecParam();
		return new BaseResult<Boolean>(true);
	}


	@ApiOperation(value="批量更新某张 job_xx 表和 job/job_online 的 execParam", notes="")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "dbTargetId", value = "目标库id", dataType = "Long"),
			@ApiImplicitParam(name = "targetTable", value = "目标表名", dataType = "String"),
			@ApiImplicitParam(name = "fieldName", value = "要更新的字段名", dataType = "String"),
			@ApiImplicitParam(name = "fieldVals", value = "指，多个用英文逗号隔开", dataType = "String")
	})
	@GetMapping("updateJobxxTableAndExecParam")
	public BaseResult<Boolean> updateJobxxTableAndExecParam(
			Long dbTargetId, String targetTable, String fieldName, String fieldVals) {
		jobUpdateService.updateJobxxTableAndExecParam(dbTargetId, targetTable, fieldName, fieldVals);
		return new BaseResult<Boolean>(true);
	}


}
