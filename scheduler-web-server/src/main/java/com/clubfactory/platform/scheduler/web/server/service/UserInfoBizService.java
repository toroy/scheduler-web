package com.clubfactory.platform.scheduler.web.server.service;

import com.clubfactory.platform.common.bean.PageUtils;
import com.clubfactory.platform.common.exception.BizException;
import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.po.GroupInfo;
import com.clubfactory.platform.scheduler.dal.po.SubGroupRel;
import com.clubfactory.platform.scheduler.dal.po.UserGroupRel;
import com.clubfactory.platform.scheduler.dal.po.UserInfo;
import com.clubfactory.platform.scheduler.web.core.service.*;
import com.clubfactory.platform.scheduler.web.core.utils.StreamApiUtils;
import com.clubfactory.platform.scheduler.web.core.vo.GroupInfoVO;
import com.clubfactory.platform.scheduler.web.core.vo.UserInfoVO;
import com.clubfactory.platform.scheduler.web.server.dto.*;
import com.clubfactory.platform.scheduler.web.server.dto.enums.UserInfoType;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.vo.BasicContactPersonVo;
import com.clubfactory.platform.scheduler.web.server.vo.ContactPersonVo;
import com.clubfactory.platform.scheduler.web.server.vo.GroupInfoVo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Service
public class UserInfoBizService {
    private final static String DEFAULT_GROUP_NAME = "默认组";

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private GroupInfoService groupInfoService;

    @Autowired
    private UserGroupRelService userGroupRelService;

    @Autowired
    private SubGroupRelService subGroupRelService;

    @Autowired
    private AlarmService alarmService;


    /**
     * 添加联系人
     * @param userInfoDto
     * @param userDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUserInfo(UserInfoDto userInfoDto, LoginUserDto userDto) {
        Assert.nonNull(userInfoDto, "联系人信息不能为空");
        Assert.nonNull(userDto, "登陆用户信息不能为空");
        // 新增非登陆用户联系人只需要保存联系人信息即可
        if (!BooleanUtils.isTrue(userInfoDto.getIsCompleteMode())) {
            saveUserInfo(userInfoDto.buildPo(userDto.getLocalUserId()));
            return;
        }
        UserInfo oldUserInfo = this.checkUserInfoExists();
        Long userInfoId = oldUserInfo != null ? oldUserInfo.getId() : null;
        Long defaultGroupId = oldUserInfo != null ? oldUserInfo.getMainGroupId() : null;
        boolean initDefaultGroup = true;
        // 创建默认组
        if (defaultGroupId == null) {
            GroupInfo defaultGroup = new GroupInfo();
            defaultGroup.setGroupName(DEFAULT_GROUP_NAME);
            defaultGroupId = this.saveUserGroup(defaultGroup);
            initDefaultGroup = false;
        }
        Assert.nonNull(defaultGroupId, "默认联系人组ID不能为空");
        // 已存在userInfo信息则保存更新
        if (userInfoId != null) {
            UserInfo userInfo = userInfoDto.buildEditPo(userInfoId, defaultGroupId);
            this.userInfoService.edit(userInfo);
        }
        // 不存在联系人则新增
        if (userInfoId == null) {
            UserInfo userInfo = userInfoDto.buildPo(userDto.getLocalUserId());
            userInfo.setMainGroupId(defaultGroupId);
            userInfoId = saveUserInfo(userInfo);
        }
        Assert.nonNull(userInfoId, "联系人ID不能为空");
        // 保存联系人组映射关系
        if (!initDefaultGroup) {
            this.saveUserGroupRel(userInfoId, defaultGroupId);
        }
    }

    /**
     * 修改联系人信息
     * @param editDto
     */
    public void editUserInfo(UserInfoEditDto editDto) {
        Assert.nonNull(editDto, "联系人信息不能为空");
        Assert.nonNull(editDto.getId(), "联系人ID不能为空");
        UserInfo userInfo = editDto.buildEditPo();
        this.userInfoService.edit(userInfo);
    }

    /**
     * 删除联系人信息
     * @param delDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void delUserInfo(UserInfoIdDto delDto) {
        Assert.notNull(delDto, "联系人信息不能为空");
        Assert.nonNull(delDto.getId(), "联系人ID不能为空");
        Long userInfoId = delDto.getId();
        this.checkPermission(userInfoId);
        this.hardDelUserGroupRef(userInfoId);
        Long defaultGroupId = this.userInfoService.getDefaultGroupIdById(userInfoId);
        this.softDelUserInfo(userInfoId);
        // 删除默认组及默认组与其下面联系人映射关系
        if (defaultGroupId == null) {
            return;
        }
        if (!this.groupIsUsingBySubscribeInfo(defaultGroupId) && !alarmService.isExist(defaultGroupId)) {
            this.removeGroupInfo(defaultGroupId);
        }
    }

    private void checkPermission(Long userInfoId) {
        UserInfo userInfoWhere = new UserInfo();
        userInfoWhere.setIsDeleted(false);
        userInfoWhere.setId(userInfoId);
        userInfoWhere.setUserId(LocalUser.getLoginUserId());
        boolean isLoginUser = this.userInfoService.get(userInfoWhere) != null;
        if (isLoginUser) {
            throw new BizException("不允许删除当前登陆用户的联系人信息");
        }
    }

    /**
     * 联系人分页查询
     * @param pagerDto
     * @return
     */
    public PageUtils<ContactPersonVo> queryContactPersonByPage(UserInfoPagerDto pagerDto) {
        Assert.nonNull(pagerDto, "分页信息不能为空");
        UserInfo userInfo = pagerDto.buildQueryPo();

        PageUtils<UserInfo> userInfoPageList = this.userInfoService.pageList(userInfo);
        if (userInfoPageList.getSize() == 0) {
            return new PageUtils<>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
        }
        List<ContactPersonVo> contactPersonVoList = userInfoPageList.getRows().stream()
                .map(ContactPersonVo::po2Vo)
                .collect(Collectors.toList());
        contactPersonVoList.forEach(user -> user.setGroups(this.getGroupStringByUserInfoId(user.getId())));

        return new PageUtils<>(contactPersonVoList, userInfoPageList.getTotalCount(),userInfoPageList.getPageSize(),userInfoPageList.getPageNo());
    }

    /**
     * 查询联系人所属组名称, 逗号分隔
     * @param userInfoId
     * @return
     */
    private String getGroupStringByUserInfoId(Long userInfoId) {
        List<GroupInfo> groupInfos = this.userGroupRelService.listGroupInfosByUserInfoId(userInfoId);
        if (CollectionUtils.isEmpty(groupInfos)) {
            return "";
        }
        return groupInfos.stream().map(GroupInfo::getGroupName).collect(Collectors.joining(","));
    }

    /**
     * 联系人信息软删除
     * @param userInfoId
     */
    private void softDelUserInfo(Long userInfoId) {
        UserInfo userInfo = new UserInfo();
        userInfo.setIsDeleted(false);
        userInfo.setId(userInfoId);
        Map<String,Object> updateParams = Maps.newHashMap();
        updateParams.put("is_deleted", true);
        updateParams.put("update_user", LocalUser.getLoginUserId());
        userInfo.setUpdateParam(updateParams);
        this.userInfoService.edit(userInfo);
    }

    /**
     * 联系人组映射关系硬删除
     * @param userInfoId
     */
    private void hardDelUserGroupRef(Long userInfoId) {
        UserGroupRel userGroupRel = new UserGroupRel();
        userGroupRel.setUserInfoId(userInfoId);
        this.userGroupRelService.remove(userGroupRel);
    }

    /**
     * 保存联系人组映射关系
     * @param userInfoId
     * @param defaultGroupId
     */
    private void saveUserGroupRel(Long userInfoId, Long defaultGroupId) {
        UserGroupRel userGroupRel = new UserGroupRel();
        userGroupRel.setGroupId(defaultGroupId);
        userGroupRel.setUserInfoId(userInfoId);
        Long userGroupRefId = this.saveUserGroupRel(userGroupRel);
        Assert.nonNull(userGroupRefId, "新增联系人组映射关系出错");
    }

    /**
     * 保存联系人组
     * @param groupInfo
     * @return
     */
    private Long saveUserGroup(GroupInfo groupInfo) {
        LocalUser.completePoInfo(groupInfo);
        this.groupInfoService.save(groupInfo);
        return groupInfo.getId();
    }

    /**
     * 保存联系人信息
     * @param userInfo
     * @return
     */
    private Long saveUserInfo(UserInfo userInfo) {
        LocalUser.completePoInfo(userInfo);
        this.userInfoService.save(userInfo);
        return userInfo.getId();
    }

    /**
     * 检查当前登陆用户联系人信息是否存在
     */
    private UserInfo checkUserInfoExists() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(LocalUser.getLoginUserId());
        userInfo.setIsDeleted(false);
        return this.userInfoService.get(userInfo);
    }

    /**
     * 保存联系人组映射关系
     * @param userGroupRel
     * @return
     */
    private Long saveUserGroupRel(UserGroupRel userGroupRel) {
        LocalUser.completePoInfo(userGroupRel);
        this.userGroupRelService.save(userGroupRel);
        return userGroupRel.getId();
    }

    /**
     * 添加联系人组信息
     * @param groupInfoDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void addGroupInfo(GroupInfoDto groupInfoDto) {
        Assert.nonNull(groupInfoDto, "联系人组信息不能为空");
        GroupInfo groupInfo = groupInfoDto.buildPo();
        this.groupInfoService.save(groupInfo);
        List<UserGroupRel> groupRels = groupInfoDto.buildRefList(groupInfo.getId());
        this.userGroupRelService.saveBatch(groupRels);
    }

    /**
     * 修改联系人组信息
     * @param groupInfoEditDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void editGroupInfo(GroupInfoEditDto groupInfoEditDto) {
        Assert.nonNull(groupInfoEditDto, "联系人组信息不能为空");
        Assert.nonNull(groupInfoEditDto.getId(), "联系人组ID不能为空");
        GroupInfo groupInfo = groupInfoEditDto.buildEditPo();
        this.groupInfoService.edit(groupInfo);

        UserGroupRel userGroupRel = new UserGroupRel();
        userGroupRel.setGroupId(groupInfo.getId());
        this.userGroupRelService.remove(userGroupRel);

        List<UserGroupRel> userGroupRels = groupInfoEditDto.buildRefList();
        if (CollectionUtils.isNotEmpty(userGroupRels)) {
            this.userGroupRelService.saveBatch(userGroupRels);
        }

    }

    /**
     * 删除联系人组信息
     * @param delDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void delGroupInfo(GroupInfoIdDto delDto) {
        Assert.nonNull(delDto, "联系人组不能为空");
        Long groupId = delDto.getId();
        Assert.nonNull(groupId, "联系人组ID不能为空");
        if (this.groupIsUsingBySubscribeInfo(groupId)) {
            throw new BizException("当前组为在用状态，请先将其从订阅列表中移除引用再尝试删除");
        }
        List<Long> existsJobIdList = this.alarmService.listJobIds(groupId);
        if (CollectionUtils.isNotEmpty(existsJobIdList)) {
            throw new BizException(String.format("以下Job正在使用该联系人组作为告警组，请先将其从中移除引用再尝试删除: %s",
                    StringUtils.join(existsJobIdList,",")));
        }
        if (isDefaultGroup(groupId)) {
            throw new BizException("不允许删除默认组");
        }
        this.removeGroupInfo(groupId);
    }

    /**
     * 删除联系人组以及该组下的用户映射关系
     * @param groupId
     */
    private void removeGroupInfo(Long groupId) {
        UserGroupRel userGroupRel = new UserGroupRel();
        userGroupRel.setGroupId(groupId);
        this.userGroupRelService.remove(userGroupRel);
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(groupId);
        this.groupInfoService.logicRemove(groupInfo);
    }

    /**
     * 查询联系人组列表
     * @param listDto
     * @return
     */
    public List<GroupInfoVo> queryGroupInfos(GroupInfoListDto listDto) {
        if (listDto == null) {
            listDto = new GroupInfoListDto();
        }
        GroupInfo groupInfo = listDto.buildPo();
        List<GroupInfoVO> groupInfoList = this.groupInfoService.list(groupInfo);

        return getGroupInfoVos(groupInfoList);
    }

    public List<GroupInfoVO> listGroupInfoByIds(List<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Lists.newArrayList();
        }
        List<GroupInfoVO> groupInfoVos = groupInfoService.listByIds(groupIds);
        return groupInfoVos;
//        return getGroupInfoVos(groupInfoVos);
    }


    @NotNull
    private List<GroupInfoVo> getGroupInfoVos(List<GroupInfoVO> groupInfoList) {
        Long defaultGroupId = getLoginUserDefaultGroupId();
        Assert.nonNull(defaultGroupId, "用户" + LocalUser.getLoginUserId() + "的默认联系人组为空");
        return groupInfoList.stream()
                .map(group -> {
                    List<UserInfo> userInfoList = this.userGroupRelService.listUserInfosByGroupId(group.getId());
                    boolean isDefaultGroup = defaultGroupId.equals(group.getId());
                    return GroupInfoVo.po2Vo(group, userInfoList, isDefaultGroup);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取当前登陆用户的默认联系人组ID
     * @return
     */
    public Long getLoginUserDefaultGroupId() {
        long loginUserId = LocalUser.getLoginUserId();
        return this.userInfoService.getDefaultGroupIdByUserId(loginUserId);
    }

    /**
     * 判断当前联系人组是否在订阅信息中使用
     * @param groupId
     * @return
     */
    private boolean groupIsUsingBySubscribeInfo(Long groupId) {
        SubGroupRel subGroupRel = new SubGroupRel();
        subGroupRel.setGroupId(groupId);
        subGroupRel.setLimitRows(1);
        // 检查订阅表是否使用该联系人组
        return this.subGroupRelService.get(subGroupRel) != null;
    }

    /**
     * 判断当前组是否为默认组
     * @param groupId
     * @return
     */
    private boolean isDefaultGroup(Long groupId) {
        UserInfo userInfoWhere = new UserInfo();
        Long loginUser = LocalUser.getLoginUserId();
        userInfoWhere.setIsDeleted(false);
        userInfoWhere.setCreateUser(loginUser);
        userInfoWhere.setUserId(loginUser);
        userInfoWhere.setMainGroupId(groupId);
        int count = this.userInfoService.count(userInfoWhere);
        return count > 0;
    }

    /**
     * 查询当前联系人组下面的联系人列表
     * @param groupUsersDto
     * @return
     */
    public List<BasicContactPersonVo> queryUserInfosFromGroup(GroupUsersDto groupUsersDto) {
        Assert.nonNull(groupUsersDto, "联系人组信息不能为空");
        boolean isInGroup = groupUsersDto.getIsInGroup() == null ? false : groupUsersDto.getIsInGroup();

        // 新增联系人组时
        if (groupUsersDto.getGroupId() == null) {
            UserInfo userInfoWhere = new UserInfo();
            userInfoWhere.setIsDeleted(false);
            userInfoWhere.setCreateUser(LocalUser.getLoginUserId());
            List<UserInfoVO> userInfoList = this.userInfoService.list(userInfoWhere);
            return CollectionUtils.isEmpty(userInfoList) ? Lists.newArrayList() :
                    userInfoList.stream().map(BasicContactPersonVo::po2Vo).collect(Collectors.toList());
        }

        List<UserInfo> inGroupList = this.userGroupRelService.listUserInfosByGroupId(groupUsersDto.getGroupId());
        if (isInGroup) {
            return CollectionUtils.isEmpty(inGroupList) ? Lists.newArrayList() :
                    inGroupList.stream().map(BasicContactPersonVo::po2Vo).collect(Collectors.toList());
        }
        UserInfo userInfoWhere = new UserInfo();
        userInfoWhere.setIsDeleted(false);
        userInfoWhere.setCreateUser(LocalUser.getLoginUserId());

        List<UserInfoVO> allUserInfo = this.userInfoService.list(userInfoWhere);
        if (CollectionUtils.isEmpty(inGroupList)) {
            return CollectionUtils.isEmpty(allUserInfo) ? Lists.newArrayList() :
                    allUserInfo.stream().map(BasicContactPersonVo::po2Vo).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(allUserInfo)) {
            return Lists.newArrayList();
        }
        List<Long> inGroupIds = inGroupList.stream().map(UserInfo::getId).collect(Collectors.toList());
        List<UserInfoVO> notInGroupList = allUserInfo.stream()
                .filter(userInfoVO -> !inGroupIds.contains(userInfoVO.getId()))
                .collect(Collectors.toList());
        return CollectionUtils.isEmpty(notInGroupList) ? Lists.newArrayList() :
                notInGroupList.stream().map(BasicContactPersonVo::po2Vo).collect(Collectors.toList());
    }

    /**
     * 修改默认联系人组
     * @param idDto
     */
    public void modifyDefaultGroup(UserInfoIdDto idDto) {
        Assert.nonNull(idDto, "联系人组信息不能为空");
        Long groupId = idDto.getId();
        Assert.nonNull(groupId, "联系人组ID不能为空");
        UserInfo userInfo = new UserInfo();
        long loginUserId = LocalUser.getLoginUserId();
        userInfo.setUserId(loginUserId);
        userInfo.setIsDeleted(false);
        userInfo.setCreateUser(loginUserId);
        Map<String,Object> updateParams = Maps.newHashMap();
        updateParams.put("main_group_id", groupId);
        userInfo.setUpdateParam(updateParams);
        this.userInfoService.edit(userInfo);
    }

    /**
     * 从联系人组中移除指定联系人
     * @param removeUserDto
     */
    public void removeUserFromGroup(RemoveUserDto removeUserDto) {
        Assert.nonNull(removeUserDto, "请求信息不能为空");
        Assert.nonNull(removeUserDto.getGroupId(), "联系人组ID不能为空");
        Assert.nonNull(removeUserDto.getUserId(), "联系人ID不能为空");
        UserGroupRel rel = removeUserDto.buildRelPo();
        this.userGroupRelService.remove(rel);
    }

    /**
     * @param createUserId : 创建人ID
     * @param createUserName： 创建人名称
     * @param userInfoList：联系人联系信息(邮箱/机器人/手机号)列表
     * @param userInfoType：联系信息类型
     * @return 联系人组ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public Long genUserGroupByUserInfoList(Long createUserId, String createUserName,
                                     List<String> userInfoList, UserInfoType userInfoType) {
        Assert.nonNull(createUserId, "创建人ID不能为空");
        Assert.nonBlank(createUserName, "创建人姓名不能为空");
        Assert.nonNull(userInfoType, "联系信息类型未指定");
        Assert.collectionNonEmpty(userInfoList, "联系信息列表为空");
        Map<String, Long> existsInfos = this.getExistsInfos(createUserId, userInfoType);
        List<Long> persistUserInfoIds = Lists.newArrayList();
        userInfoList = userInfoList.stream().distinct().collect(Collectors.toList());
        userInfoList.forEach(info -> {
            info = info.trim();
            if (existsInfos.containsKey(info)) {
                persistUserInfoIds.add(existsInfos.get(info));
            }
        });
        long random = System.currentTimeMillis() % 100000;
        String username = String.format("%s_%s", createUserName, random);
        String groupName = String.format("初始化组_%s", random);
        List<UserInfo> userInfos = userInfoList.stream().filter(item -> !existsInfos.containsKey(item.trim()))
                .map(item -> {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setCreateUser(createUserId);
                    userInfo.setUpdateUser(createUserId);
                    userInfo.setName(username);
                    switch (userInfoType) {
                        case IM_ROBOT:
                            userInfo.setImRobot(item.trim());
                            break;
                        case PHONE_NO:
                            userInfo.setPhoneNo(item.trim());
                            break;
                        case EMAIL:
                            userInfo.setEmail(item.trim());
                            break;
                        default: break;
                    }
                    return userInfo;
                }).collect(Collectors.toList());
        this.userInfoService.saveBatch(userInfos);
        userInfos.forEach(info -> {
            if (Objects.nonNull(info.getId())) {
                persistUserInfoIds.add(info.getId());
            }
        });
        List<GroupInfo> matchGroups = userGroupRelService.listGroupInfosByUserInfoIds(persistUserInfoIds);
        if (CollectionUtils.isNotEmpty(matchGroups) && matchGroups.size() == persistUserInfoIds.size()) {
            // 去重
            matchGroups = matchGroups.stream().filter(StreamApiUtils.distinctByKey(GroupInfo::getId)).collect(Collectors.toList());
            if (matchGroups.size() == 1) {
                return matchGroups.get(0).getId();
            }
        }

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setCreateUser(createUserId);
        groupInfo.setUpdateUser(createUserId);
        groupInfo.setGroupName(groupName);
        this.groupInfoService.save(groupInfo);
        Long groupId = groupInfo.getId();
        Assert.nonNull(groupId, "创建联系人组出错");

        List<UserGroupRel> userGroupRels = persistUserInfoIds.stream()
                .map(userInfoId -> {
                    UserGroupRel userGroupRel = new UserGroupRel();
                    userGroupRel.setGroupId(groupId);
                    userGroupRel.setUserInfoId(userInfoId);
                    userGroupRel.setCreateUser(createUserId);
                    userGroupRel.setUpdateUser(createUserId);
                    return userGroupRel;
                }).collect(Collectors.toList());
        this.userGroupRelService.saveBatch(userGroupRels);
        return groupId;

    }


    /**
     * 获取用于去重的已存在的联系人信息
     * @param createUserId
     * @param userInfoType
     * @return 已存在的邮箱/手机号/机器人列表 -> userInfoId列表
     */
    private Map<String, Long> getExistsInfos(Long createUserId, UserInfoType userInfoType) {
        UserInfo userInfoWhere = new UserInfo();
        userInfoWhere.setIsDeleted(false);
        userInfoWhere.setCreateUser(createUserId);
        List<UserInfoVO> userInfoVOList = this.userInfoService.list(userInfoWhere);
        final Map<String, Long> existsInfos = Maps.newHashMap();
        if (CollectionUtils.isEmpty(userInfoVOList)) {
            return existsInfos;
        }
        userInfoVOList = userInfoVOList.stream().filter(item -> Objects.nonNull(item) && Objects.nonNull(item.getCreateUser())).collect(Collectors.toList());
        switch (userInfoType) {
            case EMAIL:
                userInfoVOList.stream()
                        .filter(item -> StringUtils.isNotBlank(item.getEmail()))
                        .forEach(info -> existsInfos.put(info.getEmail().trim(), info.getId()));
                break;
            case IM_ROBOT:
                userInfoVOList.stream()
                        .filter(item -> StringUtils.isNotBlank(item.getImRobot()))
                        .forEach(info -> existsInfos.put(info.getImRobot().trim(), info.getId()));
                break;
            case PHONE_NO:
                userInfoVOList.stream()
                        .filter(item -> StringUtils.isNotBlank(item.getPhoneNo()))
                        .forEach(info -> existsInfos.put(info.getPhoneNo().trim(), info.getId()));
                break;
            default: break;
        }
        return existsInfos;
    }
}
