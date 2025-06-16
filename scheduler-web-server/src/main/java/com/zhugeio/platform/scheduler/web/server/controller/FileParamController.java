package com.zhugeio.platform.scheduler.web.server.controller;

import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.dal.enums.ScriptType;
import com.zhugeio.platform.scheduler.web.core.vo.FileParamContentVo;
import com.zhugeio.platform.scheduler.web.server.dto.ChangeDto;
import com.zhugeio.platform.scheduler.web.server.dto.FileParamPagerDto;
import com.zhugeio.platform.scheduler.web.server.login.LocalUser;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.FileParamBizService;
import com.zhugeio.platform.scheduler.web.server.vo.FileParamVo;
import com.zhugeio.platform.scheduler.web.server.vo.SimpleFileParamVo;
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

import java.util.Map;

/**
 * TODO
 *
 * @author zhoulijiang
 * @date 2025/6/16 08:25
 **/
@Api(tags = "文本参数管理")
@RestController
@RequestMapping("/fileParam")
public class FileParamController {

    @Autowired
    private FileParamBizService fileParamBizService;

    @ApiOperation(value="新增脚本", notes="新增脚本")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileParamName", value = "脚本名称", dataType = "String"),
            @ApiImplicitParam(name = "desc", value = "脚本描述", dataType = "String"),
            @ApiImplicitParam(name = "fileParamType", value = "脚本类型", dataType = "FileParamType")
    })
    @PostMapping(value = "/save")
    public BaseResult<SimpleFileParamVo> addFileParam(
            @RequestParam(value = "fileParamName") String fileParamName,
            @RequestParam(value = "desc") String desc,
            @RequestParam(value = "fileParamType") ScriptType fileParamType,
            @RequestParam(value = "isSync", required = false) Integer isSync,
            @RequestParam(value = "fileParamFile") MultipartFile fileParamFile) {
        LoginUserDto userDto = LocalUser.get();
        return new BaseResult<>(fileParamBizService.addFileParam(userDto,fileParamFile,fileParamName,desc,fileParamType));
    }

    @ApiOperation(value="重传脚本", notes="重传脚本")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "脚本ID", dataType = "Long"),
            @ApiImplicitParam(name = "fileParamName", value = "脚本名称", dataType = "String"),
            @ApiImplicitParam(name = "desc", value = "脚本描述", dataType = "String"),
            @ApiImplicitParam(name = "fileParamType", value = "脚本类型", dataType = "FileParamType")
    })
    @PostMapping(value = "/update")
    public BaseResult<Boolean> updateFileParam(
            @RequestParam(value = "id",required = true) Long id,
            @RequestParam(value = "fileParamName",required = false ) String fileParamName,
            @RequestParam(value = "desc",required = false) String desc,
            @RequestParam(value = "isSync", required = false) Integer isSync,
            @RequestParam(value = "fileParamType" ,required = false) ScriptType fileParamType,
            @RequestParam(value = "fileParamFile" ,required = false ) MultipartFile fileParamFile) {

        LoginUserDto userDto = LocalUser.get();
        fileParamBizService.updateFileParam(userDto,id,fileParamFile,fileParamName,desc,fileParamType);
        return new BaseResult<>(true);
    }

    @ApiOperation(value="脚本下载", notes="脚本下载")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "脚本ID", required = true, dataType ="Long", example = "100"),
            @ApiImplicitParam(name = "version", value = "脚本版本", dataType ="Integer", example = "5")
    })
    @GetMapping("/download")
    public ResponseEntity downFileParam(@RequestParam(value = "id") Long fileParamId,
                                     @RequestParam(required = false) Integer version) {

        try{
            Resource file = fileParamBizService.downloadFileParam(fileParamId, version);
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
    public BaseResult<Map> listFileParamLevels() {
        LoginUserDto userDto = LocalUser.get();
        return new BaseResult<>(fileParamBizService.listFileParamTypes(userDto));
    }


    @ApiOperation(value="脚本删除", notes="脚本软删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "脚本ID", dataType = "Long")
    })
    @GetMapping("/del")
    public BaseResult<Boolean> deleteFileParam(
            Long id) {
        LoginUserDto userDto = LocalUser.get();
        fileParamBizService.deleteFileParam(userDto,id);
        return new BaseResult<>(true);
    }

    @ApiOperation(value="根据ID获取脚本内容", notes="根据ID获取脚本内容")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "脚本ID", dataType = "Long"),
            @ApiImplicitParam(name = "version", value = "脚本版本", dataType = "Integer")
    })
    @GetMapping("/getContentById")
    public BaseResult<FileParamContentVo> getById(@RequestParam Long id,
                                               @RequestParam(required = false) Integer version) {
        return new BaseResult<>(fileParamBizService.getFileParamContentById(id, version));
    }


    @ApiOperation(value="脚本内容编辑", notes="脚本内容更新")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "脚本ID", dataType = "Long"),
            @ApiImplicitParam(name = "content", value = "脚本内容", dataType = "String")
    })
    @PostMapping("/edit")
    public BaseResult<Boolean> edit(
            String content,
            Long id) {
        LoginUserDto userDto = LocalUser.get();
        fileParamBizService.editFileParam(userDto,id,content);
        return new BaseResult<>(true);
    }

    @ApiOperation(value="脚本列表查询", notes="脚本列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pagerDto", value = "分页查询对象", dataType = "FileParamPagerDto"),
    })
    @PostMapping("/list")
    public BaseResult<PageUtils<FileParamVo>> listFileParams(@RequestBody FileParamPagerDto pagerDto){
        return new BaseResult<>(fileParamBizService.queryByPage(pagerDto));
    }

    @ApiOperation(value="脚本移交", notes="脚本移交")
    @PostMapping("/changeOwner")
    public BaseResult<Boolean> changeOwner(@RequestBody ChangeDto changeDto) {
        LoginUserDto userDto = LocalUser.get();
        fileParamBizService.changeFileParamOwner(changeDto, userDto);
        return new BaseResult<>(true);
    }

}
