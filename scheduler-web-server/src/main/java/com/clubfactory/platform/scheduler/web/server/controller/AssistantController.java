package com.clubfactory.platform.scheduler.web.server.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clubfactory.platform.scheduler.common.bean.BaseResult;
import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.web.core.dto.AssistantDto;
import com.clubfactory.platform.scheduler.web.core.dto.AssistantJobDto;
import com.clubfactory.platform.scheduler.web.core.dto.AssistantSimpleJobDependsDto;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.server.config.parambind.CurrentUser;
import com.clubfactory.platform.scheduler.web.server.dto.ScriptZipDto;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.service.AssistantBizService;
import com.clubfactory.platform.scheduler.web.server.service.AssistantJobBizService;
import com.clubfactory.platform.scheduler.web.server.service.ScriptBizService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;


/**
 * @author chen qian
 */
@Slf4j
@Api(tags = "一键助手")
@RequestMapping("/assistant")
@RestController
public class AssistantController {

    @Autowired
    private AssistantBizService assistantBizService;
    @Autowired
    private AssistantJobBizService assistantJobBizService;

    @Autowired
    private ScriptBizService scriptBizService;

    @ApiOperation(value = "生成任务")
    @PostMapping("/generateJob")
    public BaseResult<Boolean> generateJob(@RequestBody AssistantDto dto,
    		@CurrentUser @ApiIgnore LoginUserDto currentUser) {
    	Boolean isSuccess = assistantJobBizService.generateJob(dto, currentUser);
        return new BaseResult<Boolean>(isSuccess);
    }
    
    @ApiOperation(value = "生成任务api调用")
    @PostMapping("/generateJobByApi")
    public BaseResult<Boolean> generateJobByApi(@RequestBody AssistantJobDto dto) {
    	Boolean isSuccess = assistantJobBizService.modifyJob(dto, null);
        return new BaseResult<Boolean>(isSuccess);
    }
    
    @ApiOperation(value = "任务检查")
    @GetMapping("/checkGroup")
    public BaseResult<String> checkGroup(Long scriptId,
    		@CurrentUser @ApiIgnore LoginUserDto currentUser) {
    	String msg = assistantJobBizService.checkGroup(scriptId, currentUser);
        return new BaseResult<String>(msg);
    }


    @ApiOperation(value = "生成线上任务依赖")
    @PostMapping("/generateJobDepends")
    public BaseResult<Boolean> generateJobDepends(@RequestBody AssistantDto dto,
    		@CurrentUser @ApiIgnore LoginUserDto currentUser) {
    	Boolean isSuccess = assistantJobBizService.generateJobDepends(dto, currentUser);
        return new BaseResult<Boolean>(isSuccess);
    }
    
    @ApiOperation(value = "生成线上任务依赖api调用")
    @PostMapping("/generateJobDependsByApi")
    public BaseResult<Boolean> generateJobDependsByApi(@RequestBody AssistantSimpleJobDependsDto dto) {
    	Boolean isSuccess = assistantJobBizService.modifySimpleJobDepends(dto, null);
        return new BaseResult<Boolean>(isSuccess);
    }

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity download(String type) throws Exception {
        return assistantBizService.getFileBytesByType(type);
    }

    @ApiOperation(value = "批量脚本添加")
    @ApiImplicitParam(name = "scriptZipDto",value = "批量脚本添加dto",dataType = "ScriptZipDto")
    @PostMapping("/parseZip")
    public BaseResult<Boolean> parseZip(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                        @RequestBody ScriptZipDto scriptZipDto){
        return new BaseResult<>(scriptBizService.parseScriptZip(userDto,scriptZipDto));
    }







}
