package com.clubfactory.platform.scheduler.web.server.dqc.controller;

import com.clubfactory.platform.scheduler.common.bean.BaseResult;
import com.clubfactory.platform.scheduler.dal.po.DqcRule;
import com.clubfactory.platform.scheduler.web.core.dqc.vo.DqcRuleVO;
import com.clubfactory.platform.scheduler.web.server.dqc.service.DqcRuleBizService;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "dqc-规则")
@RequestMapping("/dqc/rule")
@RestController
public class DqcRuleController {

    @Resource
    DqcRuleBizService dqcRuleBizService;

    @ApiOperation(value="枚举列表", notes="枚举列表")
    @GetMapping("listEnums")
    public BaseResult<Map<String, Object>> listEnums(String dbName, String tableName) {
        return new BaseResult<>(dqcRuleBizService.listEnums(dbName, tableName));
    }

    @ApiOperation(value="规则列表", notes="规则列表")
    @GetMapping("list")
    public BaseResult<Map<String, List<DqcRuleVO>>> list(String dbName, String tableName) {
        LoginUserDto userDto = LocalUser.get();
        List<DqcRuleVO> rules = dqcRuleBizService.list(dbName, tableName, userDto);
        Map<String, List<DqcRuleVO>> map = Maps.newHashMap();
        map.put("rows", rules);
        return new BaseResult<>(map);
    }

    @ApiOperation(value="新增规则", notes="新增规则")
    @PostMapping("add")
    public BaseResult<Boolean> add(@RequestBody DqcRule dqcRule) {
        LoginUserDto userDto = LocalUser.get();
        return new BaseResult<>(dqcRuleBizService.save(dqcRule, userDto));
    }

    @ApiOperation(value="修改规则", notes="修改规则")
    @PostMapping("edit")
    public BaseResult<Boolean> edit(@RequestBody DqcRule dqcRule) {
        LoginUserDto userDto = LocalUser.get();
        return new BaseResult<>(dqcRuleBizService.edit(dqcRule, userDto));
    }

    @ApiOperation(value="删除规则", notes="删除规则")
    @PostMapping("delete")
    public BaseResult<Boolean> delete(@RequestBody DqcRule dqcRule) {
        LoginUserDto userDto = LocalUser.get();
        return new BaseResult<>(dqcRuleBizService.del(dqcRule.getId(), userDto));
    }

}
