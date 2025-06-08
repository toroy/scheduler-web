package com.zhugeio.platform.scheduler.web.server.controller;

import com.zhugeio.platform.scheduler.web.server.config.parambind.CurrentUser;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.LoggerService;
import com.zhugeio.platform.scheduler.web.server.service.TableLineageBizService;
import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xiejiajun
 */
@Api(tags = "系统管理")
@RestController
@RequestMapping("/sys")
public class SystemController {

    @Resource
    private LoggerService loggerService;

    @Resource
    TableLineageBizService tableLineageBizService;


    @ApiOperation(value = "获取所有Woreker机器的pubkey")
    @ApiImplicitParam(name = "osUser", value = "启动服务使用的操作系统用户", dataType = "String")
    @GetMapping("/listAllWorkerPubkey")
    public BaseResult<List<String>> listAllWorkerPubkey(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                                        String osUser){
        return new BaseResult<>(loggerService.listAllWorkerPubKey(userDto,osUser));
    }

    @GetMapping("genAllJobTableLineage")
    public Boolean genAllJobTableLineage() {
        tableLineageBizService.genAllJobTableLineage();
        return true;
    }

}
