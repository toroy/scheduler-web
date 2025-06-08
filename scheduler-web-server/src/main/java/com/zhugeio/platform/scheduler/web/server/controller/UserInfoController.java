package com.zhugeio.platform.scheduler.web.server.controller;

import com.zhugeio.platform.scheduler.web.server.config.parambind.CurrentUser;
import com.zhugeio.platform.scheduler.web.server.dto.*;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.web.server.service.UserInfoBizService;
import com.zhugeio.platform.scheduler.web.server.vo.BasicContactPersonVo;
import com.zhugeio.platform.scheduler.web.server.vo.ContactPersonVo;
import com.zhugeio.platform.scheduler.web.server.vo.GroupInfoVo;
import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.web.server.dto.*;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * @author xiejiajun
 */
@Api(tags = "联系人管理")
@RestController
@RequestMapping("/cp")
public class UserInfoController {

    @Autowired
    private UserInfoBizService userInfoBizService;

    @ApiOperation(value = "新增联系人")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userInfo",value = "新增联系人dto",dataType = "UserInfoDto")
    })
    @PostMapping("/users/add")
    public BaseResult<Boolean> addUserInfo(@CurrentUser @ApiIgnore LoginUserDto loginUser,
                                  @RequestBody UserInfoDto userInfo) {
        userInfoBizService.addUserInfo(userInfo, loginUser);
        return new BaseResult<>(true);
    }


    @ApiOperation(value = "编辑联系人")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userInfoEditDto",value = "编辑联系人dto",dataType = "UserInfoEditDto")
    })
    @PostMapping("/users/edit")
    public BaseResult<Boolean> editUserInfo(@RequestBody UserInfoEditDto userInfoEditDto) {
        userInfoBizService.editUserInfo(userInfoEditDto);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "删除联系人")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "delDto",value = "联系人ID",dataType = "UserInfoDelDto")
    })
    @PostMapping("/users/del")
    public BaseResult<Boolean> delUserInfo(@RequestBody UserInfoIdDto delDto){
        userInfoBizService.delUserInfo(delDto);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "联系人列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name",value = "姓名匹配条件",dataType = "java.lang.String"),
            @ApiImplicitParam(name = "pageNo",value = "页号",dataType = "java.lang.Integer"),
            @ApiImplicitParam(name = "pagerDto",value = "分页大小",dataType = "java.lang.Integer")
    })
    @GetMapping("/users")
    public BaseResult<PageUtils<ContactPersonVo>> listUserInfoByPage(String name,
                                                                     Integer pageNo,
                                                                     Integer pageSize){
        UserInfoPagerDto pagerDto = new UserInfoPagerDto();
        pagerDto.setName(name);
        pagerDto.setPageNo(pageNo == null ? 1 : pageNo);
        pagerDto.setPageSize(pageSize == null ? 10 : pageSize);
        return new BaseResult<>(userInfoBizService.queryContactPersonByPage(pagerDto));
    }


    @ApiOperation(value = "新增联系人组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupInfo",value = "新增联系人组dto",dataType = "GroupInfoDto")
    })
    @PostMapping("/userGroups/add")
    public BaseResult<Boolean> addUserGroupInfo(@RequestBody GroupInfoDto groupInfo) {
        userInfoBizService.addGroupInfo(groupInfo);
        return new BaseResult<>(true);
    }


    @ApiOperation(value = "编辑联系人组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupInfoEditDto",value = "编辑联系人组dto",dataType = "GroupInfoEditDto")
    })
    @PostMapping("/userGroups/edit")
    public BaseResult<Boolean> editUserGroupInfo(@RequestBody GroupInfoEditDto groupInfoEditDto) {
        userInfoBizService.editGroupInfo(groupInfoEditDto);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "删除联系人组")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "delDto",value = "联系人组ID",dataType = "GroupInfoDelDto")
    })
    @PostMapping("/userGroups/del")
    public BaseResult<Boolean> delGroupInfo(@RequestBody GroupInfoIdDto delDto){
        userInfoBizService.delGroupInfo(delDto);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "联系人组列表")
    @ApiImplicitParam(name = "groupName",value = "批量查询条件",dataType = "java.lang.String")
    @GetMapping("/userGroups")
    public BaseResult<Map<String,List<GroupInfoVo>>> listGroupInfos(String groupName){
        GroupInfoListDto listDto = new GroupInfoListDto();
        listDto.setGroupName(groupName);
        Map<String, List<GroupInfoVo>> results = Maps.newHashMap();
        results.put("rows", userInfoBizService.queryGroupInfos(listDto));
        return new BaseResult<>(results);
    }

    @ApiOperation(value = "联系人组用户概览列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId",value = "联系人组ID",dataType = "java.lang.Long"),
            @ApiImplicitParam(name = "isInGroup",value = "联系人组ID",dataType = "java.lang.Boolean")
    })
    @GetMapping("/userGroups/users")
    public BaseResult<Map<String,List<BasicContactPersonVo>>> listUserInfoFromGroup(Long groupId,
                                                                                    Boolean isInGroup){
        GroupUsersDto groupUsersDto = new GroupUsersDto();
        groupUsersDto.setGroupId(groupId);
        groupUsersDto.setIsInGroup(isInGroup);
        Map<String, List<BasicContactPersonVo>> results = Maps.newHashMap();
        results.put("rows", userInfoBizService.queryUserInfosFromGroup(groupUsersDto));
        return new BaseResult<>(results);
    }

    @ApiOperation(value = "联系人组设为默认组")
    @ApiImplicitParam(name = "idDto",value = "联系人组ID",dataType = "UserInfoIdDto")
    @PostMapping("/userGroups/setDefault")
    public BaseResult<Boolean> setDefaultGroup(@RequestBody UserInfoIdDto idDto){
        userInfoBizService.modifyDefaultGroup(idDto);
        return new BaseResult<>(true);
    }

    @ApiOperation(value = "从联系人组移除指定联系人")
    @ApiImplicitParam(name = "removeUserDto",value = "联系人组ID",dataType = "RemoveUserDto")
    @PostMapping("/userGroups/delUser")
    public BaseResult<Boolean> removeUserFromGroup(@RequestBody RemoveUserDto removeUserDto){
        userInfoBizService.removeUserFromGroup(removeUserDto);
        return new BaseResult<>(true);
    }

}
