package com.clubfactory.platform.scheduler.web.server.controller;

import com.clubfactory.platform.common.bean.BaseResult;
import com.clubfactory.platform.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.web.server.dto.*;
import com.clubfactory.platform.scheduler.web.server.service.SubscribeBizService;
import com.clubfactory.platform.scheduler.web.server.vo.SimpleDataSourceVo;
import com.clubfactory.platform.scheduler.web.server.vo.SimpleSubscribeVo;
import com.clubfactory.platform.scheduler.web.server.vo.SubscribeVo;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author xiejiajun
 */
@Api(tags = "我的订阅")
@RestController
@RequestMapping("/subscribes")
public class SubscribeController {

    @Autowired
    private SubscribeBizService subscribeBizService;

    @ApiOperation(value = "新增订阅")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "subscribeInfoDto",value = "新增订阅dto",dataType = "SubscribeInfoDto")
    })
    @PostMapping("/add")
    public BaseResult<Boolean> addSubscribeInfo(@RequestBody SubscribeInfoDto subscribeInfoDto) {
        subscribeBizService.addSubscribeInfo(subscribeInfoDto);
        return new BaseResult<>(true);
    }


    @ApiOperation(value = "修改订阅")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "subscribeInfoEditDto",value = "修改订阅dto",dataType = "SubscribeInfoEditDto")
    })
    @PostMapping("/edit")
    public BaseResult<Boolean> editSubscribeInfo(@RequestBody SubscribeInfoEditDto subscribeInfoEditDto) {
        subscribeBizService.editSubscribeInfo(subscribeInfoEditDto);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "取消订阅")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unSubscribeDto",value = "订阅信息ID列表",dataType = "UnSubscribeDto")
    })
    @PostMapping("/cancel")
    public BaseResult<Boolean> cancelSubscribe(@RequestBody UnSubscribeDto unSubscribeDto){
        subscribeBizService.cancelSubscribe(unSubscribeDto);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "订阅列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dataSource",value = "数据源匹配条件",dataType = "java.lang.String"),
            @ApiImplicitParam(name = "dbName",value = "数据库名称匹配条件",dataType = "java.lang.String"),
            @ApiImplicitParam(name = "tableName",value = "表名称匹配条件",dataType = "java.lang.String"),
            @ApiImplicitParam(name = "pageNo",value = "页号",dataType = "java.lang.Integer"),
            @ApiImplicitParam(name = "pagerDto",value = "分页大小",dataType = "java.lang.Integer")
    })
    @GetMapping
    public BaseResult<PageUtils<SubscribeVo>> listSubscribeInfoByPage(String dataSource,
                                                                      String dbName,
                                                                      String tableName,
                                                                      Integer pageNo,
                                                                      Integer pageSize){
        SubscribePagerDto pagerDto = new SubscribePagerDto();
        pagerDto.setDataSource(dataSource);
        pagerDto.setDbName(dbName);
        pagerDto.setTableName(tableName);
        pagerDto.setPageNo(pageNo == null ? 1 : pageNo);
        pagerDto.setPageSize(pageSize == null ? 10 : pageSize);
        return new BaseResult<>(subscribeBizService.queryByPage(pagerDto));
    }

    @ApiOperation(value = "订阅列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dataSource",value = "数据源匹配条件",dataType = "java.lang.String"),
            @ApiImplicitParam(name = "dbName",value = "数据库名称匹配条件",dataType = "java.lang.String"),
            @ApiImplicitParam(name = "tableName",value = "表名称匹配条件",dataType = "java.lang.String"),
            @ApiImplicitParam(name = "pageNo",value = "页号",dataType = "java.lang.Integer"),
            @ApiImplicitParam(name = "pagerDto",value = "分页大小",dataType = "java.lang.Integer")
    })
    @GetMapping("/listSubscribeByPage")
    public BaseResult<PageUtils<SubscribeVo>> listSubscribeByPage(String dataSource,
                                                                      String dbName,
                                                                      String tableName,
                                                                      Integer pageNo,
                                                                      Integer pageSize){
        SubscribePagerDto pagerDto = new SubscribePagerDto();
        pagerDto.setDataSource(dataSource);
        pagerDto.setDbName(dbName);
        pagerDto.setTableName(tableName);
        pagerDto.setPageNo(pageNo == null ? 1 : pageNo);
        pagerDto.setPageSize(pageSize == null ? 10 : pageSize);
        return new BaseResult<>(subscribeBizService.queryByPage(pagerDto));
    }

    @ApiOperation(value = "数据源列表")
    @ApiImplicitParam(name = "dsName",value = "数据源模糊匹配条件",dataType = "java.lang.String")
    @GetMapping("/dataSources")
    public BaseResult<Map<String,List<SimpleDataSourceVo>>> listDataSources(String dsName){
        Map<String, List<SimpleDataSourceVo>> results = Maps.newHashMap();
        results.put("rows", subscribeBizService.queryDataSources(dsName));
        return new BaseResult<>(results);
    }

    @ApiOperation(value = "表列表")
    @ApiImplicitParam(name = "tableName",value = "模糊查询条件",dataType = "java.lang.String")
    @GetMapping("/tables")
    public BaseResult<Map<String,List<SimpleSubscribeVo>>> listTables(String tableName){
        Map<String, List<SimpleSubscribeVo>> results = Maps.newHashMap();
        results.put("rows", subscribeBizService.queryTables(tableName));
        return new BaseResult<>(results);
    }
}
