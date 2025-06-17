package com.zhugeio.platform.scheduler.web.server.controller;

import com.zhugeio.platform.scheduler.web.server.config.parambind.CurrentUser;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.SchedulerNodeBizService;
import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.web.core.dto.SchedulerNodeDto;
import com.zhugeio.platform.scheduler.web.core.dto.SchedulerNodeEditDto;
import com.zhugeio.platform.scheduler.web.server.dto.SchedulerNodePagerDto;
import com.zhugeio.platform.scheduler.web.server.vo.SchedulerNodeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

/**
 * @author xiejiajun
 */
@Api(tags = "调度机管理")
@RestController
@RequestMapping("/node")
public class SchedulerNodeController {

    @Autowired
    private SchedulerNodeBizService schedulerNodeBizService;



    @ApiOperation(value = "新增调度机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeDto",value = "调度机新增dto",dataType = "SchedulerNodeDto")
    })
    @PostMapping("/save")
    public BaseResult<Boolean> addNode(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                       @RequestBody SchedulerNodeDto nodeDto) {

        schedulerNodeBizService.addNode(userDto,nodeDto);
        return new BaseResult<>(true);
    }


    @ApiOperation(value = "修改调度机信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "editDto",value = "调度机信息dto",dataType = "SchedulerNodeEditDto")
    })
    @PostMapping("/edit")
    public BaseResult<Boolean> editNode(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                        @RequestBody SchedulerNodeEditDto editDto){
        schedulerNodeBizService.editNode(userDto,editDto);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "删除调度机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "调度机ID",dataType = "Long")
    })
    @GetMapping("/del")
    public BaseResult<Boolean> delNode(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                        Long id){
        schedulerNodeBizService.delNode(userDto,id);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "根据ID查询调度机信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "调度机ID",dataType = "Long")
    })
    @GetMapping("/getById")
    public BaseResult<SchedulerNodeVo> getById(Long id){
        return new BaseResult<>(schedulerNodeBizService.getById(id));
    }

    @ApiOperation(value = "调度机列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pagerDto" ,value = "分页查询条件",dataType = "SchedulerNodePagerDto")
    })
    @PostMapping("/list")
    public BaseResult<PageUtils<SchedulerNodeVo>> listSchedulerNodes(@RequestBody  SchedulerNodePagerDto pagerDto){
        return new BaseResult<>(schedulerNodeBizService.queryByPage(pagerDto));
    }

    @ApiOperation(value = "调度机下拉列表枚举")
    @GetMapping("/dicts")
    public BaseResult<Map> listWorkerEnums(){
        return new BaseResult<>(schedulerNodeBizService.getEnums());
    }

    @ApiOperation(value = "调度机复制")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "被复制的节点ID",dataType = "Long")
    })
    @GetMapping("/copy")
    public BaseResult<Boolean> copySchedulerNode(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                                 Long id){
        schedulerNodeBizService.copyNode(id,userDto.getLocalUserId());
        return new BaseResult<>(true);
    }


}
