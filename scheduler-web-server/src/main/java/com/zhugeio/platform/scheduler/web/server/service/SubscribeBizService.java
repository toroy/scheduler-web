package com.zhugeio.platform.scheduler.web.server.service;

import com.zhugeio.platform.scheduler.web.server.dto.SubscribeInfoDto;
import com.zhugeio.platform.scheduler.web.server.dto.SubscribeInfoEditDto;
import com.zhugeio.platform.scheduler.web.server.dto.SubscribePagerDto;
import com.zhugeio.platform.scheduler.web.server.dto.UnSubscribeDto;
import com.zhugeio.platform.scheduler.web.server.dto.mapper.SubscribeDtoMapper;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.common.bean.BaseResult;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.common.util.BeanUtil;
import com.zhugeio.platform.meta.client.dto.TableDto;
import com.zhugeio.platform.meta.client.dto.TableSimpleDto;
import com.zhugeio.platform.meta.client.enums.DbType;
//import com.clubfactory.platform.meta.client.service.MetaClientService;
import com.zhugeio.platform.meta.client.utils.DbUtil;
import com.zhugeio.platform.scheduler.dal.enums.SubscribeType;
import com.zhugeio.platform.scheduler.dal.po.*;
import com.zhugeio.platform.scheduler.web.client.dto.CheckSubscribeDto;
import com.zhugeio.platform.scheduler.web.client.dto.SimpleSubscribeDto;
import com.zhugeio.platform.scheduler.web.core.service.*;
import com.zhugeio.platform.scheduler.web.core.vo.AlertSubVO;
import com.zhugeio.platform.scheduler.web.core.vo.CollectDbVO;
import com.zhugeio.platform.scheduler.web.core.vo.SubGroupRelVO;
import com.zhugeio.platform.scheduler.web.server.login.LocalUser;
import com.zhugeio.platform.scheduler.web.server.vo.SimpleDataSourceVo;
import com.zhugeio.platform.scheduler.web.server.vo.SimpleSubscribeVo;
import com.zhugeio.platform.scheduler.web.server.vo.SubscribeVo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Slf4j
@Service
public class SubscribeBizService {

    @Resource
    private SubGroupRelService subGroupRelService;

    @Resource
    private AlertSubService alertSubService;

    @Resource
    private UnsubInfoService unsubInfoService;

    @Resource
    private CollectDbService collectDbService;

    @Resource
    private UserService userService;

    @Resource
    private UserInfoService userInfoService;

//    @Resource
//    private MetaClientService metaClientService;


    /**
     * 新增订阅信息
     * @param subscribeInfoDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void addSubscribeInfo(SubscribeInfoDto subscribeInfoDto) {
        Assert.nonNull(subscribeInfoDto, "订阅信息不能为空");
        Long subscribeId = this.getExistAlertInfoId(subscribeInfoDto.buildSimplePo());
        if (subscribeId == null) {
            AlertSub alertSub = subscribeInfoDto.buildPo();
            this.alertSubService.save(alertSub);
            subscribeId = alertSub.getId();
        }
        Assert.nonNull(subscribeId, "新增订阅失败, 订阅ID为空");
        List<Long> subscribedGroupList = this.getSubscribedGroupList(subscribeId, LocalUser.getLoginUserId());
        List<SubGroupRel> subGroupRels = subscribeInfoDto.buildRefList(subscribeId, subscribedGroupList);
        this.subGroupRelService.saveBatch(subGroupRels);
    }

    /**
     * 获取已存在的订阅信息ID，防止同一用户重复生成同一张表的订阅信息
     * @param alertSubFilter
     * @return
     */
    private Long getExistAlertInfoId(AlertSub alertSubFilter) {
        AlertSub alertSub = this.alertSubService.get(alertSubFilter);
        if (alertSub == null || alertSub.getId() == null) {
            return null;
        }
        return alertSub.getId();
    }

    /**
     * 获取关注指定订阅信息的组ID列表
     * @param subscribeId
     * @return
     */
    private List<Long> getSubscribedGroupList(Long subscribeId, Long createUser) {
        SubGroupRel subGroupRelFiler = new SubGroupRel();
        subGroupRelFiler.setIsDeleted(false);
        subGroupRelFiler.setSubId(subscribeId);
        subGroupRelFiler.setCreateUser(createUser);
        List<SubGroupRelVO> subGroupRelVOList = this.subGroupRelService.list(subGroupRelFiler);
        if (CollectionUtils.isEmpty(subGroupRelVOList)) {
            return Lists.newArrayList();
        }
        return subGroupRelVOList.stream().filter(item -> Objects.nonNull(item.getGroupId())).map(SubGroupRel::getGroupId).collect(Collectors.toList());
    }

    /**
     * 修改订阅信息
     * @param subscribeInfoEditDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void editSubscribeInfo(SubscribeInfoEditDto subscribeInfoEditDto) {
        Assert.nonNull(subscribeInfoEditDto, "订阅信息不能为空");
        Assert.nonNull(subscribeInfoEditDto.getId(), "订阅ID不能为空");

        SubGroupRel subGroupRel = new SubGroupRel();
        subGroupRel.setSubId(subscribeInfoEditDto.getId());
        this.subGroupRelService.remove(subGroupRel);

        List<SubGroupRel> subGroupRels = subscribeInfoEditDto.buildRefList();
        if (CollectionUtils.isNotEmpty(subGroupRels)) {
            this.subGroupRelService.saveBatch(subGroupRels);
        }
    }

    /**
     * 取消订阅
     * @param unSubscribeDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscribe(UnSubscribeDto unSubscribeDto) {
        Assert.nonNull(unSubscribeDto, "订阅信息不能为空");
        final LoginUserDto loginUser = LocalUser.get();
        final Long loginUserId = loginUser.getLocalUserId();
        this.cancelSubscribe(unSubscribeDto.getIdList(), loginUserId);
    }

    /**
     * 取消订阅
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelSubscribe(SimpleSubscribeDto subscribeDto) {
        Assert.nonNull(subscribeDto, "订阅信息不能为空");
        Assert.nonNull(subscribeDto.getSubscribeUid(), "订阅人UID不能为空");
        final String userUid = subscribeDto.getSubscribeUid();
        final Long userId = this.userService.getUserIdByUid(userUid);
        Assert.nonNull(userId, "未发现uid为" + userUid + "的盖亚用户");
        AlertSub alertSubWhere = new AlertSub();
        alertSubWhere.setIsDeleted(false);
        alertSubWhere.setDataSource(subscribeDto.getDataSource());
        alertSubWhere.setDbName(subscribeDto.getDbName());
        alertSubWhere.setTableName(subscribeDto.getTableName());
        alertSubWhere.setPicId(subscribeDto.getPicUid());
        List<AlertSubVO> alertSubList = this.alertSubService.list(alertSubWhere);
        if (CollectionUtils.isEmpty(alertSubList)) {
            return;
        }
        List<Long> subIdList = alertSubList.stream().filter(item -> item.getId() != null)
                .map(AlertSubVO::getId)
                .collect(Collectors.toList());
        this.cancelSubscribe(subIdList, userId);
    }

    /**
     * @param subIdList
     * @param userId
     */
    private void cancelSubscribe(List<Long> subIdList, Long userId) {
        Assert.collectionNonEmpty(subIdList, "订阅ID列表不能为空");
        AlertSub alertSubWhere = new AlertSub();
        alertSubWhere.setIsDeleted(false);
        alertSubWhere.setIds(subIdList);
        List<AlertSubVO> alertSubs = this.alertSubService.list(alertSubWhere);
        if (CollectionUtils.isEmpty(alertSubs)) {
            return;
        }
        List<UnsubInfo> unsubInfos = alertSubs.stream()
                .map(subInfo -> unsubRecordTransformer(subInfo, userId))
                .collect(Collectors.toList());
        this.unsubInfoService.saveBatch(unsubInfos);
        this.batchDeleteSubGroupRel(subIdList);
        this.alertSubService.remove(alertSubWhere);
    }

    /**
     * 根据订阅信息转换取消订阅记录
     * @param alertSub
     * @param userId
     * @return
     */
    private UnsubInfo unsubRecordTransformer(AlertSub alertSub, Long userId) {
        UnsubInfo unsubInfo = new UnsubInfo();
        BeanUtil.copyBeanNotNull2Bean(alertSub, unsubInfo);
        unsubInfo.setUpdateUser(userId);
        unsubInfo.setCreateUser(userId);
        unsubInfo.setSubUserId(userId);
        return unsubInfo;
    }

    /**
     * 根据订阅ID列表批量删除组订阅映射信息
     * @param subIdList
     */
    private void batchDeleteSubGroupRel(List<Long> subIdList) {
        this.subGroupRelService.batchRemove("sub_id", subIdList);
    }

    /**
     * 订阅信息分页查询
     * @param pagerDto
     * @return
     */
    public PageUtils<SubscribeVo> queryByPage(SubscribePagerDto pagerDto) {
        Assert.nonNull(pagerDto, "分页信息不能为空");
        AlertSub alertSubWhere = pagerDto.buildQueryPo();

        PageUtils<AlertSub> alertSubPageList = this.alertSubService.pageList(alertSubWhere);
        if (alertSubPageList.getSize() == 0) {
            return new PageUtils<>(com.google.common.collect.Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
        }
        Map<String,String> dataSourceMap = this.listDataSources();
        Map<String, String> tableOwnerMap = this.listTableOwnerMap(alertSubPageList.getRows());
        List<SubscribeVo> subscribeVoList = alertSubPageList.getRows().stream()
                .filter(item -> Objects.nonNull(dataSourceMap.get(item.getDataSource())))
                .map(alertSub -> {
                    String picNameKey = DbUtil.getTableKey(alertSub.getDataSource(), alertSub.getDbName(), alertSub.getTableName());
                    String picName = tableOwnerMap.getOrDefault(picNameKey, "");
                    String dataSourceDesc = dataSourceMap.getOrDefault(alertSub.getDataSource(), "废弃数据源");
                    List<GroupInfo> groupInfos = this.subGroupRelService.listGroupInfosBySubId(alertSub.getId());
                    return SubscribeVo.po2Vo(alertSub, picName, dataSourceDesc, groupInfos);
                }).collect(Collectors.toList());
        return new PageUtils<>(subscribeVoList, alertSubPageList.getTotalCount(),alertSubPageList.getPageSize(),alertSubPageList.getPageNo());
    }

    /**
     * 通过Dubbo接口查询数据源列表, dsName用于模糊匹配
     * @param dsName
     * @return
     */
    public List<SimpleDataSourceVo> queryDataSources(String dsName) {
        BaseResult<List<String>> dsList;
        try {
            //dsList = this.metaClientService.listDbSource(dsName);
        } catch (Exception e) {
            log.error("获取数据源列表失败", e);
            throw new BizException("元数据服务异常");
        }
//        this.checkResponse(dsList);
//        if (CollectionUtils.isEmpty(dsList.getBody())) {
//            return Lists.newArrayList();
//        }
//        Map<String,String> dataSourceMap = this.listDataSources();
//        return dsList.getBody().stream()
//                .filter(dsHost -> Objects.nonNull(dataSourceMap.get(dsHost)))
//                .map(dsHost -> SimpleDataSourceVo.dto2Vo(dsHost, dataSourceMap))
//                .collect(Collectors.toList());
        return Lists.newArrayList();
    }

    /**
     * 通过Dubbo接口查询Table列表, tableName用于模糊匹配
     * @param tableName
     * @return
     */
    public List<SimpleSubscribeVo> queryTables(String tableName) {
        BaseResult<List<TableDto>> tableDtos;
        try {
            //tableDtos = this.metaClientService.listTables(tableName);
        } catch (Exception e) {
            log.error("获取Table列表失败", e);
            throw new BizException("元数据服务异常");
        }

        //this.checkResponse(tableDtos);
//        if (CollectionUtils.isEmpty(tableDtos.getBody())) {
//            return Lists.newArrayList();
//        }
//        Map<String,String> dataSourceMap = this.listDataSources();
//        return tableDtos.getBody().stream()
//                .filter(item -> Objects.nonNull(dataSourceMap.get(item.getDbHost())))
//                .map(dto -> SimpleSubscribeVo.dto2Vo(dto, dataSourceMap))
//                .collect(Collectors.toList());
        return Lists.newArrayList();
    }

    /**
     * 检查dubbo调用响应值
     * @param baseResult
     */
    private void checkResponse(BaseResult baseResult) {
        if (baseResult == null) {
            log.error("元数据服务返回值为空NULL");
            throw new BizException("元数据服务返回值为空NULL");
        }
        if (!baseResult.isSuccess()) {
            log.error("元数据服务调用失败:{}", baseResult.getMessage());
            throw new BizException("元数据服务调用失败: " + baseResult.getMessage());
        }
    }

    /**
     * 获取所有数据源列表
     * @return
     */
    private Map<String,String> listDataSources() {
        Map<String,String> dataSourceMap = Maps.newHashMap();
        CollectDb collectDbWhere = new CollectDb();
        collectDbWhere.setIsDeleted(false);
        List<CollectDbVO> collectDbList = this.collectDbService.list(collectDbWhere);
        if (CollectionUtils.isEmpty(collectDbList)) {
            return dataSourceMap;
        }
        collectDbList.forEach(collectDbVO -> dataSourceMap.put(collectDbVO.getDbHost(), collectDbVO.getDsName()));
        String hiveDataSource = DbUtil.getDbSource("", DbType.HIVE);
        dataSourceMap.put(hiveDataSource, hiveDataSource);
        return dataSourceMap;
    }

    /**
     * 获取表责任人列表
     * @param alertSubList
     * @return
     */
    private Map<String,String> listTableOwnerMap(List<AlertSub> alertSubList) {
        List<String> tableIdentifierKeys = alertSubList.stream().map(SubscribeDtoMapper::mapToTableSimpleDto)
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());
        BaseResult<Map<String, String>> tableOwnerMap;
//        try {
//            tableOwnerMap = this.metaClientService.getUserNameByTableKey(tableIdentifierKeys);
//        } catch (Exception e) {
//            log.error("获取责任人列表失败", e);
//            return Maps.newHashMap();
//        }
//        if (tableOwnerMap == null) {
//            log.error("元数据服务返回值为NULL");
//            return Maps.newHashMap();
//        }
//        if (!tableOwnerMap.isSuccess()) {
//            log.error("元数据服务调用失败: {}", tableOwnerMap.getMessage());
//            return Maps.newHashMap();
//        }
//        if (MapUtils.isEmpty(tableOwnerMap.getBody())) {
//            return Maps.newHashMap();
//        }
//        return tableOwnerMap.getBody();
        return Maps.newHashMap();

    }

    /**
     * 依赖订阅处理
     * @param tableSimpleDtos
     */
    public void depSubscribe(List<TableSimpleDto> tableSimpleDtos) {
        Assert.nonNull(tableSimpleDtos, "订阅信息不能为空");
        tableSimpleDtos = this.filterUnSubList(tableSimpleDtos);
        tableSimpleDtos = this.filterExistAlertInfo(tableSimpleDtos);
        if (CollectionUtils.isEmpty(tableSimpleDtos)) {
            return;
        }
        Map<Long, Long> userIdToDefaultGroupIdMap = this.listDefaultGroupIds();
        if (userIdToDefaultGroupIdMap.size() == 0) {
            log.info("默认组列表为空，终止依赖订阅");
            return;
        }
        List<AlertSub> alertSubList = tableSimpleDtos.stream()
                .map(tableSimpleDto -> {
                    AlertSub alertSub = new AlertSub();
                    alertSub.setCreateUser(tableSimpleDto.getJobCreateUser());
                    alertSub.setUpdateUser(tableSimpleDto.getJobCreateUser());
                    alertSub.setDataSource(tableSimpleDto.getDbSource());
                    alertSub.setDbName(tableSimpleDto.getDbName());
                    alertSub.setTableName(tableSimpleDto.getName());
                    alertSub.setPicId(tableSimpleDto.getUid());
                    alertSub.setSubType(SubscribeType.DEP_SUBSCRIBE);
                    return alertSub;
                })
                .collect(Collectors.toList());
        this.alertSubService.saveBatch(alertSubList);
        List<Long> alertSubIds = alertSubList.stream().map(AlertSub::getId).collect(Collectors.toList());
        Assert.collectionNonEmpty(alertSubIds, "订阅信息批量保存出错，未返回主键ID列表");
        if (alertSubIds.size() != alertSubList.size()) {
            throw new BizException("批量保存的订阅信息数和返回的ID数不一致");
        }
        List<SubGroupRel> subGroupRels = alertSubList.stream()
                .filter(sub -> sub != null && userIdToDefaultGroupIdMap.get(sub.getCreateUser()) != null)
                .map(sub -> {
                    SubGroupRel subGroupRel = new SubGroupRel();
                    subGroupRel.setSubId(sub.getId());
                    subGroupRel.setGroupId(userIdToDefaultGroupIdMap.get(sub.getCreateUser()));
                    subGroupRel.setUpdateUser(sub.getCreateUser());
                    subGroupRel.setCreateUser(sub.getCreateUser());
                    return subGroupRel;
                })
                .collect(Collectors.toList());
        this.subGroupRelService.saveBatch(subGroupRels);
    }

    /**
     * 获取userId -> defaultGroupId的映射关系
     * @return
     */
    private Map<Long, Long> listDefaultGroupIds() {
        UserInfo userInfoWhere = new UserInfo();
        userInfoWhere.setIsDeleted(false);
        List<UserInfo> userInfoList = this.userInfoService.listLoginUserInfos(userInfoWhere);
        Map<Long, Long> userIdToDefaultGroupIdMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(userInfoList)) {
            return userIdToDefaultGroupIdMap;
        }
        userInfoList.stream().filter(userInfo -> userInfo.getUserId() != null)
                .forEach(userInfo -> userIdToDefaultGroupIdMap.put(userInfo.getUserId(), userInfo.getMainGroupId()));
        return userIdToDefaultGroupIdMap;
    }

    /**
     * 过滤掉取消订阅的信息
     * @param tableSimpleDtos
     * @return
     */
    private List<TableSimpleDto> filterUnSubList(List<TableSimpleDto> tableSimpleDtos) {
        int pageSize = 1000;
        int pageNo = 1;

        UnsubInfo unsubInfoPager = new UnsubInfo();
        unsubInfoPager.setIsDeleted(false);
        unsubInfoPager.setPageSize(pageSize);

        do {
            try {
                unsubInfoPager.setPageNo(pageNo);
                PageUtils<UnsubInfo> unsubInfos = this.unsubInfoService.pageList(unsubInfoPager);
                if (unsubInfos.getSize() == 0 || CollectionUtils.isEmpty(unsubInfos.getRows())) {
                    return tableSimpleDtos;
                }
                tableSimpleDtos = this.filterUnSubList(tableSimpleDtos, unsubInfos.getRows());
                if (CollectionUtils.isEmpty(tableSimpleDtos)) {
                    return tableSimpleDtos;
                }
                if (unsubInfos.getSize() == 0) {
                    break;
                }
                if (CollectionUtils.isEmpty(unsubInfos.getRows())) {
                    break;
                }
                if (unsubInfos.getRows().size() < pageSize) {
                    break;
                }
            } catch (Exception e){
                log.error(e.getMessage());
            } finally {
                pageNo += 1;
            }

        } while (true);
        return tableSimpleDtos;
    }

    /**
     * 过滤掉已经存在的订阅信息，防止同一个人重复订阅某张表
     * @param tableSimpleDtos
     * @return
     */
    private List<TableSimpleDto> filterExistAlertInfo(List<TableSimpleDto> tableSimpleDtos) {
        int pageSize = 1000;
        int pageNo = 1;

        AlertSub alertSubFilter = new AlertSub();
        alertSubFilter.setIsDeleted(false);
        alertSubFilter.setPageNo(pageNo);
        alertSubFilter.setPageSize(pageSize);

        do {
            try {
                PageUtils<AlertSub> existAlertInfos = this.alertSubService.pageList(alertSubFilter);
                if (existAlertInfos.getSize() == 0 || CollectionUtils.isEmpty(existAlertInfos.getRows())) {
                    return tableSimpleDtos;
                }
                if (CollectionUtils.isEmpty(tableSimpleDtos)) {
                    return tableSimpleDtos;
                }
                tableSimpleDtos = tableSimpleDtos.stream().filter(tableSimpleDto -> {
                    if (tableSimpleDto == null) {
                        return false;
                    }
                    for (AlertSub alertSub : existAlertInfos.getRows()) {
                        if (isExistItem(tableSimpleDto, alertSub)){
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());

                alertSubFilter.setPageNo(pageNo);
                if (existAlertInfos.getRows().size() < pageSize) {
                    break;
                }
            } catch (Exception e){
                log.error(e.getMessage());
            } finally {
                pageNo += 1;
            }

        } while (true);
        return tableSimpleDtos;

    }

    /**
     * 判断是否为已存在的条目
     * @param tableSimpleDto
     * @param alertSub
     * @return
     */
    private boolean isExistItem(TableSimpleDto tableSimpleDto, AlertSub alertSub) {
        if (alertSub == null) {
            return false;
        }
        String dataSource = tableSimpleDto.getDbSource();
        String dbName = tableSimpleDto.getDbName();
        String tableName = tableSimpleDto.getName();
        if (dataSource == null || dbName == null || tableName == null) {
            return true;
        }
        if (!dataSource.equals(alertSub.getDataSource())) {
            return false;
        }
        if (!dbName.equals(alertSub.getDbName())) {
            return false;
        }
        return tableName.equals(alertSub.getTableName());
    }

    /**
     * 批量过滤取消订阅记录
     * @param tableSimpleDtos
     * @param unsubInfoList
     * @return
     */
    private List<TableSimpleDto> filterUnSubList(List<TableSimpleDto> tableSimpleDtos, List<UnsubInfo> unsubInfoList) {
        if (CollectionUtils.isEmpty(unsubInfoList) || CollectionUtils.isEmpty(tableSimpleDtos)) {
            return tableSimpleDtos;
        }
        return tableSimpleDtos.stream().filter(tableSimpleDto -> {
            for (UnsubInfo unsubInfo : unsubInfoList) {
                if (isRequireSkipItem(tableSimpleDto, unsubInfo)){
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 检查是否需要跳过
     * @param tableSimpleDto
     * @param unsubInfo
     * @return
     */
    private boolean isRequireSkipItem(TableSimpleDto tableSimpleDto, UnsubInfo unsubInfo) {
        if (tableSimpleDto == null) {
            return true;
        }
        if (unsubInfo == null) {
            return false;
        }
        String dataSource = tableSimpleDto.getDbSource();
        String dbName = tableSimpleDto.getDbName();
        String tableName = tableSimpleDto.getName();
        Long subscriberId = tableSimpleDto.getJobCreateUser();
        if (dataSource == null || dbName == null || tableName == null || subscriberId == null) {
            return true;
        }
        if (!dataSource.equals(unsubInfo.getDataSource())) {
            return false;
        }
        if (!dbName.equals(unsubInfo.getDbName())) {
            return false;
        }
        if (!tableName.equals(unsubInfo.getTableName())) {
            return false;
        }
        return subscriberId.equals(unsubInfo.getSubUserId());
    }

    /**
     * 默认组订阅处理
     * @param subscribeDto
     */
    @Transactional(rollbackFor = Exception.class)
    public void defaultSubscribe(SimpleSubscribeDto subscribeDto) {
        Assert.nonNull(subscribeDto, "订阅信息不能为空");
        AlertSub alertSub = new AlertSub();
        Long subscribeUserId = this.userService.getUserIdByUid(subscribeDto.getSubscribeUid());
        Assert.nonNull(subscribeUserId, "未找到对应的用户ID");
        Long subUserDefaultGroupId = this.userInfoService.getDefaultGroupIdByUserId(subscribeUserId);
        Assert.nonNull(subUserDefaultGroupId, "未找到对应的默认联系人组ID");
        alertSub.setCreateUser(subscribeUserId);
        alertSub.setUpdateUser(subscribeUserId);
        alertSub.setDataSource(subscribeDto.getDataSource());
        alertSub.setDbName(subscribeDto.getDbName());
        alertSub.setTableName(subscribeDto.getTableName());
        alertSub.setIsDeleted(false);
        Long subscribeId = this.getExistAlertInfoId(alertSub);
        if (subscribeId == null) {
            alertSub.setPicId(subscribeDto.getPicUid());
            alertSub.setSubType(SubscribeType.MANUAL_SUBSCRIBE);
            this.alertSubService.save(alertSub);
            subscribeId = alertSub.getId();
        }
        Assert.nonNull(subscribeId, "新增订阅失败, 订阅ID为空");
        List<Long> existGroupList = this.getSubscribedGroupList(subscribeId, subscribeUserId);
        if (existGroupList.contains(subUserDefaultGroupId)) {
            // subscribe -> userGroup map had exist
            return;
        }
        SubGroupRel subGroupRel = new SubGroupRel();
        subGroupRel.setSubId(subscribeId);
        subGroupRel.setGroupId(subUserDefaultGroupId);
        subGroupRel.setUpdateUser(subscribeUserId);
        subGroupRel.setCreateUser(subscribeUserId);
        this.subGroupRelService.save(subGroupRel);
    }

    /**
     * 根据UID检查指定用户是否已订阅指定表
     * @param checkSubscribeDto
     * @return
     */
    public boolean isSubscribe(CheckSubscribeDto checkSubscribeDto) {
        Assert.nonNull(checkSubscribeDto, "请求参数不能为空");
        checkSubscribeDto.checkParams();
        AlertSub alertSubFilter = new AlertSub();
        alertSubFilter.setIsDeleted(false);
        alertSubFilter.setDataSource(checkSubscribeDto.getDataSource());
        alertSubFilter.setDbName(checkSubscribeDto.getDbName());
        alertSubFilter.setTableName(checkSubscribeDto.getTableName());
        Long subscriberUserId= this.userService.getUserIdByUid(checkSubscribeDto.getSubscriberUid());
        alertSubFilter.setCreateUser(subscriberUserId);
        List<AlertSubVO> alertSubList =  this.alertSubService.list(alertSubFilter);
        return CollectionUtils.isNotEmpty(alertSubList);
    }
}
