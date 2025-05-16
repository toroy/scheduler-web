package com.bigdata.platform.scheduler.web.server.dqc.controller;

import com.bigdata.platform.scheduler.web.server.dqc.dto.ContentDto;
import com.bigdata.platform.scheduler.web.server.dqc.dto.DqcJobDto;
import com.bigdata.platform.scheduler.web.server.dqc.dto.DqcRunDto;
import com.bigdata.platform.scheduler.web.server.dqc.dto.DqcTaskDto;
import com.bigdata.platform.scheduler.web.server.dqc.service.DqcBizService;
import com.bigdata.platform.scheduler.web.server.dqc.vo.DqcDetailVo;
import com.bigdata.platform.scheduler.web.server.dqc.vo.DqcRunVo;
import com.bigdata.platform.scheduler.web.server.dqc.vo.DqcTableRuleVo;
import com.bigdata.platform.scheduler.web.server.dqc.vo.DqcTaskVo;
import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.web.server.service.AlarmBizService;
import com.bigdata.platform.scheduler.common.bean.BaseResult;
import com.bigdata.platform.scheduler.common.bean.PageUtils;
import com.bigdata.platform.scheduler.common.bean.Pager;
import com.bigdata.platform.scheduler.web.server.login.LocalUser;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "dqc")
@RequestMapping("/dqc")
@RestController
public class DqcController {

    @Resource
    DqcBizService dqcBizService;
    @Resource
    AlarmBizService alarmBizService;

    @ApiOperation(value="关联任务", notes="关联任务")
    @PostMapping("relJob")
    public BaseResult<Boolean> add(@RequestBody DqcJobDto dqcJobDto) {
        LoginUserDto userDto = LocalUser.get();
        return new BaseResult<>(dqcBizService.relJob(dqcJobDto.getDbName(), dqcJobDto.getTableName(), dqcJobDto.getJobId(), userDto));
    }

    @ApiOperation(value="表监控规则列表", notes="表监控规则列表")
    @GetMapping("listTableRulesByPage")
    public BaseResult<PageUtils<DqcTableRuleVo>> listTableRulesByPage(String dbName, String tableName, Pager pager) {
        return new BaseResult<>(dqcBizService.listByPage(dbName, tableName, pager.getPageNo(), pager.getPageSize()));
    }

    @ApiOperation(value="任务监控列表", notes="任务监控列表")
    @GetMapping("listTaskRulesByPage")
    public BaseResult<PageUtils<DqcTaskVo>> listTaskRulesByPage(DqcTaskDto dqcTaskDto) {
        return new BaseResult<>(dqcBizService.listTaskRulesByPage(dqcTaskDto));
    }

    @ApiOperation(value="试跑", notes="试跑")
    @PostMapping("run")
    public BaseResult<Map<String, List<DqcRunVo>>> run(@RequestBody DqcRunDto dqcRunDto) {
        LoginUserDto userDto = LocalUser.get();
        List<DqcRunVo> vos = dqcBizService.run(dqcRunDto, userDto);
        Map<String, List<DqcRunVo>> map = Maps.newHashMap();
        map.put("rows", vos);
        return new BaseResult<>(map);
    }

    @ApiOperation(value="dqc详情", notes="dqc详情")
    @GetMapping("detail")
    public BaseResult<DqcDetailVo> detail(String dbName, String tableName) {
        LoginUserDto userDto = LocalUser.get();
        return new BaseResult<DqcDetailVo>(dqcBizService.detail(dbName, tableName, userDto));
    }

    @ApiOperation(value="枚举列表", notes="枚举列表")
    @GetMapping("listEnums")
    public BaseResult<Map<String, List<ContentDto>>> listEnums() {
        return new BaseResult<>(dqcBizService.listEnums());
    }

    @GetMapping("fixAlarm")
    public BaseResult<Boolean> fixAlarm() {
        alarmBizService.run();
        return new BaseResult<>(true);
    }
}
