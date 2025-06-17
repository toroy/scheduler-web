package com.zhugeio.platform.scheduler.web.server.controller;

import java.util.Map;

import com.zhugeio.platform.scheduler.web.server.config.parambind.CurrentUser;
import com.zhugeio.platform.scheduler.web.server.dto.DataSourceTestDto;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.DataSourceBizService;
import com.zhugeio.platform.scheduler.web.server.vo.CipherTextVo;
import com.zhugeio.platform.scheduler.web.server.vo.ConnVo;
import com.zhugeio.platform.scheduler.web.server.vo.DataSourceVo;
import com.zhugeio.platform.scheduler.web.server.dto.CipherTextDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.web.core.dto.DataSourceDto;
import com.zhugeio.platform.scheduler.web.core.dto.DataSourceEditDto;
import com.zhugeio.platform.scheduler.web.server.dto.DataSourcePagerDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author xiejiajun
 */
@Api(tags = "数据接入")
@RestController
@RequestMapping("/dataSource")
public class DataSourceController {

    @Autowired
    private DataSourceBizService dataSourceBizService;

    @ApiOperation(value="新增数据源", notes="添加JDBC数据源")
    @ApiImplicitParam(name = "dataSourceDto", value = "数据源信息", dataType = "DataSourceDto")
    @PostMapping("/save")
    public BaseResult<Boolean> save(@CurrentUser @ApiIgnore LoginUserDto currentUser,
                                    @RequestBody DataSourceDto dataSourceDto) {
        dataSourceBizService.addDataSource(dataSourceDto,currentUser.getLocalUserId());
        return new BaseResult<>(true);
    }


    @ApiOperation(value="更新数据源", notes="更新已有数据源")
    @ApiImplicitParam(name = "editDto", value = "数据源信息", dataType = "DataSourceEditDto")
    @PostMapping("/edit")
    public BaseResult<Boolean> edit(@CurrentUser @ApiIgnore LoginUserDto currentUser,
                                    @RequestBody DataSourceEditDto editDto) {
        dataSourceBizService.editDataSource(editDto,currentUser);
        return new BaseResult<>(true);
    }

    @ApiOperation(value="删除数据源", notes="软删除已有数据源")
    @ApiImplicitParam(name = "id", value = "数据源ID", dataType = "Long")
    @PostMapping("/del")
    public BaseResult<Boolean> del(@CurrentUser @ApiIgnore LoginUserDto currentUser,
                                   Long id) {
        dataSourceBizService.delDataSource(id,currentUser);
        return new BaseResult<>(true);
    }

    @ApiOperation(value="数据源分页查询", notes="数据源分页查询")
    @ApiImplicitParam(name = "pagerDto", value = "查询条件", dataType = "DataSourcePagerDto")
    @PostMapping("/list")
    public BaseResult<PageUtils<DataSourceVo>> queryByPage(@RequestBody DataSourcePagerDto pagerDto){
        PageUtils<DataSourceVo> data = dataSourceBizService.queryByPage(pagerDto);
        return new BaseResult<>(data);
    }

    @ApiOperation(value="根据ID查询数据源", notes="根据ID查询数据源")
    @ApiImplicitParam(name = "id", value = "数据源ID", dataType = "Long")
    @GetMapping("/getById")
    public BaseResult<DataSourceVo> getById(Long id){
        return new BaseResult<>(dataSourceBizService.getDataSourceById(id));
    }

    @ApiOperation(value="根据connId(数据源名称)查询数据源信息", notes="根据connId(数据源名称)查询数据源信息")
    @ApiImplicitParam(name = "connId", value = "数据源connId", dataType = "String")
    @GetMapping("/getByConnId")
    public BaseResult<ConnVo> getByConnId(String connId){
        return new BaseResult<>(dataSourceBizService.getConnInfoByConnId(connId));
    }

    @ApiOperation(value="下拉选择枚举获取", notes="下拉选择枚举获取")
    @GetMapping("/dicts")
    public BaseResult<Map> listDicts(){
        return new BaseResult<>(dataSourceBizService.getEnums());
    }

    @ApiOperation(value="数据源测试", notes="数据源测试")
    @ApiImplicitParam(name = "testDto", value = "测试数据源信息", dataType = "DataSourceTestDto")
    @PostMapping("/test")
    public BaseResult<Map> testDS(@RequestBody DataSourceTestDto testDto){
        return new BaseResult<>(dataSourceBizService.testDataSourceConn(testDto));
    }

    @ApiOperation(value="数据源复制", notes="数据源复制")
    @ApiImplicitParam(name = "id", value = "被复制的数据源ID", dataType = "Long")
    @GetMapping("/copy")
    public BaseResult<Boolean> copyDS(Long id,@CurrentUser @ApiIgnore LoginUserDto currentUser){
        dataSourceBizService.copyDataSource(id,currentUser.getLocalUserId());
        return new BaseResult<>(true);
    }


    @ApiOperation(value = "密码加密")
    @GetMapping("/encrypt")
    @ApiImplicitParam(name = "message",value = "待加密数据",dataType = "String")
    public BaseResult<CipherTextVo> encrypt(String message){
        return new BaseResult<>(dataSourceBizService.encrypt(message));
    }


    @ApiOperation(value = "密码解密")
    @PostMapping("/decrypt")
    @ApiImplicitParam(name = "cipherText",value = "密文信息",dataType = "CipherTextDto")
    public BaseResult<String> decrypt(@RequestBody CipherTextDto cipherText){
        return new BaseResult<>(dataSourceBizService.decrypt(cipherText));
    }
}
