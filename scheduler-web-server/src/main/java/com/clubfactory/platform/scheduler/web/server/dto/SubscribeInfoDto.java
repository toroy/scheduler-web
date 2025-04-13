package com.clubfactory.platform.scheduler.web.server.dto;

import com.beust.jcommander.internal.Lists;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.enums.SubscribeType;
import com.clubfactory.platform.scheduler.dal.po.AlertSub;
import com.clubfactory.platform.scheduler.dal.po.SubGroupRel;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Data
public class SubscribeInfoDto {

    /**
     * 数据源名称
     */
    private String dataSource;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 表责任人uid
     */
    private String picUid;

    /**
     * 订阅信息使用的联系人组ID列表
     */
    private List<Long> userGroups;

    public AlertSub buildPo() {
        Assert.collectionNonEmpty(userGroups, "订阅组列表不能为空");
        AlertSub alertSub = new AlertSub();
        LocalUser.completePoInfo(alertSub);
        alertSub.setDataSource(dataSource);
        alertSub.setDbName(dbName);
        alertSub.setTableName(tableName);
        alertSub.setPicId(picUid);
        alertSub.setSubType(SubscribeType.MANUAL_SUBSCRIBE);
        return alertSub;
    }

    public AlertSub buildSimplePo() {
        Assert.nonBlank(dataSource, "数据源不能为空");
        Assert.nonBlank(dbName, "数据库名称不能为空");
        Assert.nonBlank(tableName, "表名不能为空");
        AlertSub alertSub = new AlertSub();
        LocalUser.completePoInfo(alertSub);
        alertSub.setDataSource(dataSource);
        alertSub.setDbName(dbName);
        alertSub.setTableName(tableName);
        alertSub.setIsDeleted(false);
        return alertSub;
    }

    public List<SubGroupRel> buildRefList(final Long subscribeId, List<Long> subscribedGroupList) {
        if (CollectionUtils.isEmpty(userGroups)) {
            return Lists.newArrayList();
        }
        Long loginUser = LocalUser.getLoginUserId();
        return this.userGroups.stream()
                .filter(item -> !subscribedGroupList.contains(item))
                .map(groupId -> {
                    SubGroupRel rel = new SubGroupRel();
                    rel.setGroupId(groupId);
                    rel.setSubId(subscribeId);
                    rel.setCreateUser(loginUser);
                    rel.setUpdateUser(loginUser);
                    return rel;
                }).collect(Collectors.toList());
    }
}
