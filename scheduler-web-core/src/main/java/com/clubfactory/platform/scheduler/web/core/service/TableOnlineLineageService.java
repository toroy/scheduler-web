package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.dao.TableOnlineLineageMapper;
import com.clubfactory.platform.scheduler.dal.dto.SubscribeDto;
import com.clubfactory.platform.scheduler.dal.enums.LineageTypeEnum;
import com.clubfactory.platform.scheduler.dal.po.TableLineage;
import com.clubfactory.platform.scheduler.dal.po.TableOnlineLineage;
import com.clubfactory.platform.scheduler.web.core.constant.Fields;
import com.clubfactory.platform.scheduler.web.core.vo.TableOnlineLineageVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TableOnlineLineageService extends BaseNewService<TableOnlineLineageVO,TableOnlineLineage> {

    @Resource
    TableOnlineLineageMapper tableOnlineLineageMapper;


    @PostConstruct
    public void init(){
        setBaseMapper(tableOnlineLineageMapper);
    }

    public List<TableOnlineLineageVO> listByJobIds(List<Long> jobIds) {
    	Assert.collectionNotEmpty(jobIds, "任务id");
    	
    	TableOnlineLineage tableOnlineLineage = new TableOnlineLineage();
    	tableOnlineLineage.setIds(jobIds);
    	tableOnlineLineage.setQueryListFieldName(Fields.JOB_ID);
    	return this.list(tableOnlineLineage);
    }

    public List<TableOnlineLineageVO> listAll() {
        TableOnlineLineage tableOnlineLineage = new TableOnlineLineage();
        tableOnlineLineage.setIsDeleted(false);
        return this.list(tableOnlineLineage);
    }

    public void logicRemoveByJobIds(List<Long> jobIds) {
        TableOnlineLineage tolExample = new TableOnlineLineageVO();
        tolExample.setQueryListFieldName(Fields.JOB_ID);
        tolExample.setIds(jobIds);
        tolExample.setUpdateTime(new Date());
        tableOnlineLineageMapper.logicRemove(tolExample);
    }

    public void saveBatchFrom(List<TableLineage> tls) {
        List<TableOnlineLineage> tols = new ArrayList<>();
        Date date = new Date();
        for (TableLineage tl : tls) {
            TableOnlineLineage tol = new TableOnlineLineageVO();
            BeanUtil.copyBeanNotNull2Bean(tl, tol);
            tol.setId(null);
            tol.setLineageId(tl.getId());
            tol.setCreateUser(tl.getCreateUser());
            tol.setUpdateUser(tl.getUpdateUser());
            tol.setUpdateTime(date);
            tols.add(tol);
        }
        if (CollectionUtils.isNotEmpty(tols)) {
            tableOnlineLineageMapper.saveBatch(tols);
        }
    }

    /**
     * 获取目标表的所有任务信息
     *
     * @param dbHost 数据源
     * @param dbName 库名
     * @param tableName 表名
     * @return 列表消息
     */
    public List<TableOnlineLineageVO> listByChild(String dbHost, String dbName, String tableName) {
    	Assert.notBlank(dbHost, "dbHost");
    	Assert.notBlank(dbName, "dbName");
    	Assert.notBlank(tableName, "tableName");

    	TableOnlineLineage tableOnlineLineage = new TableOnlineLineage();
    	tableOnlineLineage.setDbHost(dbHost);
        tableOnlineLineage.setType(LineageTypeEnum.CHILD);
    	tableOnlineLineage.setDbName(dbName);
    	tableOnlineLineage.setTableName(tableName);
    	return this.list(tableOnlineLineage);
    }
    
    /**
     * 获取目标表的所有任务信息
     *
     * @param dbName 库名
     * @param tableName 表名
     * @return 列表消息
     */
    public List<TableOnlineLineageVO> listByChild(String dbName, String tableName) {
    	Assert.notBlank(dbName, "dbName");
    	Assert.notBlank(tableName, "tableName");

    	TableOnlineLineage tableOnlineLineage = new TableOnlineLineage();
        tableOnlineLineage.setType(LineageTypeEnum.CHILD);
    	tableOnlineLineage.setDbName(dbName);
    	tableOnlineLineage.setTableName(tableName);
    	return this.list(tableOnlineLineage);
    }

    /**
     * 根据JobId列表批量获取Job的父/子依赖信息
     * @param jobIdList
     * @param lineageType
     * @return
     */
    public List<TableOnlineLineageVO> listByJobIds(List<Long> jobIdList, LineageTypeEnum lineageType) {
        Assert.collectionNonEmpty(jobIdList, "Job Id列表为空");
        lineageType = lineageType == null ? LineageTypeEnum.PARENT : lineageType;

        TableOnlineLineage tableOnlineLineageWhere = new TableOnlineLineage();
        tableOnlineLineageWhere.setIsDeleted(false);
        tableOnlineLineageWhere.setIds(jobIdList);
        tableOnlineLineageWhere.setQueryListFieldName(Fields.JOB_ID);
        tableOnlineLineageWhere.setType(lineageType);
        return this.list(tableOnlineLineageWhere);
    }

    /**
     * 根据ID列表拉取依赖订阅所需信息
     * @param idList
     * @return
     */
    public List<SubscribeDto> listSubscribeInfosByIds(List<Long> idList) {
        return this.tableOnlineLineageMapper.listSubscribeInfos(idList);
    }

}
