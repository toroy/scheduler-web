package com.bigdata.platform.scheduler.web.server.controller;

import com.bigdata.platform.scheduler.web.server.config.parambind.CurrentUser;
import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.web.server.service.ClusterBizService;
import com.bigdata.platform.scheduler.web.server.vo.ClusterVo;
import com.bigdata.platform.scheduler.common.bean.BaseResult;
import com.bigdata.platform.scheduler.common.bean.PageUtils;
import com.bigdata.platform.scheduler.web.core.dto.ClusterDto;
import com.bigdata.platform.scheduler.web.core.dto.ClusterEditDto;
import com.bigdata.platform.scheduler.web.server.dto.ClusterPagerDto;
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
@Api(tags = "集群管理")
@RequestMapping("/cluster")
@RestController
public class ClusterController {

    @Autowired
    private ClusterBizService clusterBizService;


    @ApiOperation(value = "新增集群")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterDto",value = "新增集群dto",dataType = "ClusterDto")
    })
    @PostMapping("/save")
    public BaseResult<Boolean> addCluster(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                          @RequestBody ClusterDto clusterDto){
        clusterBizService.addCluster(userDto,clusterDto);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "修改集群信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "editDto",value = "修改集群信息dto",dataType = "ClusterEditDto")
    })
    @PostMapping("/edit")
    public BaseResult<Boolean> editCluster(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                           @RequestBody ClusterEditDto editDto){
        clusterBizService.editCluster(userDto,editDto);
        return  new BaseResult<>(true);
    }


    @ApiOperation(value = "删除集群信息(软删除)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "集群ID",dataType = "Long")
    })
    @GetMapping("/del")
    public BaseResult<Boolean> delCluster(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                          Long id){
        clusterBizService.delCluster(userDto,id);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "根据集群ID查询集群信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "集群ID",dataType = "Long")
    })
    @GetMapping("/getById")
    public BaseResult<ClusterVo> getClusterById(Long id){
        return new BaseResult<>(clusterBizService.getClusterById(id));
    }


    @ApiOperation(value = "集群列表")
    @ApiImplicitParam(name = "pagerDto",value = "分页查询条件",dataType = "ClusterPagerDto")
    @PostMapping("/list")
    public BaseResult<PageUtils<ClusterVo>> listByPage(@RequestBody ClusterPagerDto pagerDto){
        return new BaseResult<>(clusterBizService.queryByPage(pagerDto));
    }


    @ApiOperation(value = "集群管理下拉枚举")
    @GetMapping("/dicts")
    public BaseResult<Map> listClusterEnums(){
        return new BaseResult<>(clusterBizService.getEnums());
    }
}
