package com.clubfactory.platform.scheduler.web.server.controller;

import com.clubfactory.platform.common.bean.BaseResult;
import com.clubfactory.platform.common.bean.PageUtils;
import com.clubfactory.platform.common.exception.BizException;
import com.clubfactory.platform.scheduler.dal.enums.ScriptType;
import com.clubfactory.platform.scheduler.web.server.config.parambind.CurrentUser;
import com.clubfactory.platform.scheduler.web.server.dto.ChangeDto;
import com.clubfactory.platform.scheduler.web.server.dto.ScriptPagerDto;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.service.ScriptBizService;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.vo.ScriptContentVo;
import com.clubfactory.platform.scheduler.web.server.vo.ScriptVo;
import com.clubfactory.platform.scheduler.web.server.vo.SimpleScriptVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

/**
 * @author xiejiajun
 */
@Api(tags = "脚本管理")
@RestController
@RequestMapping("/script")
public class ScriptController {


    @Autowired
    private ScriptBizService scriptBizService;

    @ApiOperation(value="新增脚本", notes="新增脚本")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scriptName", value = "脚本名称", dataType = "String"),
            @ApiImplicitParam(name = "desc", value = "脚本描述", dataType = "String"),
            @ApiImplicitParam(name = "scriptType", value = "脚本类型", dataType = "ScriptType")
    })
    @PostMapping(value = "/save")
    public BaseResult<SimpleScriptVo> addScript(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                                @RequestParam(value = "scriptName") String scriptName,
                                                @RequestParam(value = "desc") String desc,
                                                @RequestParam(value = "scriptType") ScriptType scriptType,
                                                @RequestParam(value = "isSync", required = false) Integer isSync,
                                                @RequestParam(value = "scriptFile") MultipartFile scriptFile) {
        return new BaseResult<>(scriptBizService.addScript(userDto,scriptFile,scriptName,desc,scriptType));
    }

    @ApiOperation(value="重传脚本", notes="重传脚本")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "脚本ID", dataType = "Long"),
            @ApiImplicitParam(name = "scriptName", value = "脚本名称", dataType = "String"),
            @ApiImplicitParam(name = "desc", value = "脚本描述", dataType = "String"),
            @ApiImplicitParam(name = "scriptType", value = "脚本类型", dataType = "ScriptType")
    })
    @PostMapping(value = "/update")
    public BaseResult<Boolean> updateScript(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                            @RequestParam(value = "id",required = true) Long id,
                                            @RequestParam(value = "scriptName",required = false ) String scriptName,
                                            @RequestParam(value = "desc",required = false) String desc,
                                            @RequestParam(value = "isSync", required = false) Integer isSync,
                                            @RequestParam(value = "scriptType" ,required = false) ScriptType scriptType,
                                            @RequestParam(value = "scriptFile" ,required = false ) MultipartFile scriptFile) {

        scriptBizService.updateScript(userDto,id,scriptFile,scriptName,desc,scriptType);
        return new BaseResult<>(true);
    }

    @ApiOperation(value="脚本下载", notes="脚本下载")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "脚本ID", required = true, dataType ="Long", example = "100"),
            @ApiImplicitParam(name = "version", value = "脚本版本", dataType ="Integer", example = "5")
    })
    @GetMapping("/download")
    public ResponseEntity downScript(@RequestParam(value = "id") Long scriptId,
                                     @RequestParam(required = false) Integer version) {

        try{
            Resource file = scriptBizService.downloadScript(scriptId, version);
            if (file == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("脚本不存在或脚本文件不可读");
            }
            return ResponseEntity.ok()
                    .contentLength(file.contentLength())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("文件下载出错: %s",e.getMessage()));
        }
    }


    @ApiOperation(value="脚本类型枚举", notes="脚本级别枚举")
    @GetMapping("/types")
    public BaseResult<Map> listScriptLevels(@CurrentUser @ApiIgnore LoginUserDto userDto) {
        return new BaseResult<>(scriptBizService.listScriptTypes(userDto));
    }


    @ApiOperation(value="脚本删除", notes="脚本软删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "脚本ID", dataType = "Long")
    })
    @GetMapping("/del")
    public BaseResult<Boolean> deleteScript(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                            Long id) {
        scriptBizService.deleteScript(userDto,id);
        return new BaseResult<>(true);
    }

    @ApiOperation(value="根据ID获取脚本内容", notes="根据ID获取脚本内容")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "脚本ID", dataType = "Long"),
            @ApiImplicitParam(name = "version", value = "脚本版本", dataType = "Integer")
    })
    @GetMapping("/getContentById")
    public BaseResult<ScriptContentVo> getById(@RequestParam Long id,
                                               @RequestParam(required = false) Integer version) {
        return new BaseResult<>(scriptBizService.getScriptContentById(id, version));
    }


    @ApiOperation(value="脚本内容编辑", notes="脚本内容更新")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "脚本ID", dataType = "Long"),
            @ApiImplicitParam(name = "content", value = "脚本内容", dataType = "String")
    })
    @PostMapping("/edit")
    public BaseResult<Boolean> edit(@CurrentUser @ApiIgnore LoginUserDto userDto,
                                               String content,
                                               Long id) {
        scriptBizService.editScript(userDto,id,content);
        return new BaseResult<>(true);
    }

    @ApiOperation(value="脚本列表查询", notes="脚本列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pagerDto", value = "分页查询对象", dataType = "ScriptPagerDto"),
    })
    @PostMapping("/list")
    public BaseResult<PageUtils<ScriptVo>> listScripts(@RequestBody ScriptPagerDto pagerDto){
        return new BaseResult<>(scriptBizService.queryByPage(pagerDto));
    }

    @ApiOperation(value="脚本移交", notes="脚本移交")
    @PostMapping("/changeOwner")
    public BaseResult<Boolean> changeOwner(@RequestBody ChangeDto changeDto) {
    	LoginUserDto userDto = LocalUser.get();
    	scriptBizService.changeScriptOwner(changeDto, userDto);
        return new BaseResult<>(true);
    }

}
