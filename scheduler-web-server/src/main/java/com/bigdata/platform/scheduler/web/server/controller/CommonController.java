package com.bigdata.platform.scheduler.web.server.controller;

import com.bigdata.platform.scheduler.web.server.service.MacroVarBizService;
import com.bigdata.platform.scheduler.web.server.service.UserBizService;
import com.bigdata.platform.scheduler.common.bean.BaseResult;
import com.bigdata.platform.scheduler.web.server.service.TeamBizService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiejiajun
 */
@Api(tags = "公共接口模块")
@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private UserBizService userBizService;

    @Autowired
    private TeamBizService teamBizService;

    @Autowired
    private MacroVarBizService macroVarBizService;


    @ApiOperation(value = "获取部门列表")
    @GetMapping("/departs/list")
    public BaseResult<Map> getDepartments() {
        return new BaseResult<>(
                new HashMap(1) {
                    {
                        put("rows", teamBizService.listDepartments());
                    }
                });
    }


    @ApiOperation(value = "获取常量值")
    @ApiImplicitParam(name = "valName", value = "常量名称", dataType = "String")
    @GetMapping("/constant/get")
    public BaseResult<String> getConstant(String valName) {
        return new BaseResult<>(macroVarBizService.getVarByName(valName));
    }
}
