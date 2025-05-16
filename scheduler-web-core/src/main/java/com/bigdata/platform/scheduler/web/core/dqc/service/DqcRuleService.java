package com.bigdata.platform.scheduler.web.core.dqc.service;

import com.bigdata.platform.scheduler.common.bean.PageUtils;
import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.dal.dao.DqcRuleMapper;
import com.bigdata.platform.scheduler.dal.po.DqcRule;
import com.bigdata.platform.scheduler.web.core.dqc.vo.DqcRuleVO;
import com.bigdata.platform.scheduler.web.core.service.BaseNewService;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DqcRuleService extends BaseNewService<DqcRuleVO, DqcRule> {

    @Resource
    DqcRuleMapper dqcRuleMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(dqcRuleMapper);
    }

    public Boolean updateRelJobId(String dbName, String tableName, Long relJobId) {
        Assert.notBlank(dbName);
        Assert.notBlank(tableName);
        Assert.notNull(relJobId);

        DqcRule dqcRule = new DqcRule();
        dqcRule.setTableName(tableName);
        dqcRule.setDbName(dbName);
        dqcRule.setIsDeleted(false);
        Map<String, Object> updateParam = Maps.newHashMap();
        dqcRule.setUpdateParam(updateParam);
        updateParam.put("rel_job_id", relJobId);
        this.edit(dqcRule);
        return true;
    }

    public Boolean updateJobId(Long id, Long jobId) {
        Assert.notNull(id);
        Assert.notNull(jobId);

        DqcRule dqcRule = new DqcRule();
        dqcRule.setId(id);
        Map<String, Object> updateParam = Maps.newHashMap();
        dqcRule.setUpdateParam(updateParam);
        updateParam.put("job_id", jobId);
        this.edit(dqcRule);
        return true;
    }

    public Boolean cleanRelJobIds(List<Long> relJobIds) {
        Assert.collectionNotEmpty(relJobIds, "relJobIds");

        DqcRule dqcRule = new DqcRule();
        dqcRule.setIds(relJobIds);
        dqcRule.setQueryListFieldName("rel_job_id");
        dqcRule.setUpdateTime(new Date());
        Map<String, Object> updateParam = Maps.newHashMap();
        dqcRule.setUpdateParam(updateParam);
        updateParam.put("rel_job_id", null);
        dqcRuleMapper.editIfNull(dqcRule);
        return true;
    }




    public void updateDqcRule(DqcRule dqcRule, Long userId) {
        DqcRule rule = new DqcRule();
        Map<String, Object> map = Maps.newHashMap();
        map.put("update_user", userId);
        map.put("compare", dqcRule.getCompare());
        map.put("description", dqcRule.getDescription());
        map.put("check_type", dqcRule.getCheckType());
        map.put("sample", dqcRule.getSample());
        map.put("check_mode", dqcRule.getCheckMode());
        map.put("filter", dqcRule.getFilter());
        map.put("field", dqcRule.getField());
        map.put("user_sql", dqcRule.getUserSql());
        map.put("is_block", dqcRule.getIsBlock());
        map.put("expect_value", dqcRule.getExpectValue());
        map.put("upper_threshold", dqcRule.getUpperThreshold());
        map.put("lower_threshold", dqcRule.getLowerThreshold());
        map.put("name", dqcRule.getName());
        map.put("connector_type", dqcRule.getConnectorType());
        rule.setUpdateParam(map);
        rule.setId(dqcRule.getId());
        this.edit(rule);
    }

    public Long getRelJobId(String dbName, String tableName) {
        List<DqcRuleVO> ruleVOS = this.list(dbName, tableName);
        if (CollectionUtils.isEmpty(ruleVOS)) {
            return null;
        }
        return ruleVOS.stream().map(DqcRuleVO::getRelJobId).findFirst().orElse(null);
    }

    public List<Long> listJobIds(String dbName, String tableName) {
        return this.list(dbName, tableName).stream().map(DqcRuleVO::getJobId).collect(Collectors.toList());
    }

    public List<DqcRuleVO> listByPartitionId(Long partitionId) {
        Assert.notNull(partitionId);

        DqcRule dqcRule = new DqcRule();
        dqcRule.setIsDeleted(false);
        dqcRule.setDqcPartitionId(partitionId);
        return this.list(dqcRule);
    }

    public List<DqcRuleVO> list(String dbName, String tableName) {
        Assert.notBlank(dbName);
        Assert.notBlank(tableName);

        DqcRule dqcRule = new DqcRule();
        dqcRule.setTableName(tableName);
        dqcRule.setDbName(dbName);
        dqcRule.setIsDeleted(false);
        return this.list(dqcRule);
    }

    public List<DqcRuleVO> list(List<Long> jobIds) {
        if (CollectionUtils.isEmpty(jobIds)) {
            return Lists.newArrayList();
        }

        DqcRule dqcRule = new DqcRule();
        dqcRule.setIds(jobIds);
        dqcRule.setQueryListFieldName("job_id");
        dqcRule.setIsDeleted(false);
        return this.list(dqcRule);
    }

    public List<DqcRuleVO> list(String dbName, String tableName, Boolean isBlock) {

        DqcRule dqcRule = new DqcRule();
        dqcRule.setTableName(tableName);
        dqcRule.setIsBlock(isBlock);
        dqcRule.setDbName(dbName);
        dqcRule.setIsDeleted(false);
        return this.list(dqcRule);
    }

    public List<DqcRuleVO> listByTableNames(List<String> tableNames) {
        if (CollectionUtils.isEmpty(tableNames)) {
            return Lists.newArrayList();
        }

        DqcRule dqcRule = new DqcRule();
        dqcRule.setIdsString(tableNames);
        dqcRule.setQueryListFieldName("table_name");
        dqcRule.setIsDeleted(false);
        return this.list(dqcRule);
    }

    public Boolean deleteById(Long id, Long userId) {
        DqcRule dqcRule = new DqcRule();
        dqcRule.setId(id);
        dqcRule.setUpdateUser(userId);
        this.logicRemove(dqcRule);
        return true;
    }

    public DqcRule getById(Long id) {
        Assert.notNull(id);
        DqcRule dqcRule = new DqcRule();
        dqcRule.setId(id);
        dqcRule.setIsDeleted(false);
        return this.get(dqcRule);
    }

    public List<Map<String, Object>> listMap() {
        DqcRule po = new DqcRule();
        // 查近一个月多一周的数据
        po.setStartDate(LocalDate.now().minusMonths(1).minusWeeks(1).toString());
        return dqcRuleMapper.listTargetTaskIds(po);
    }

    public PageUtils<Map<String, Object>> page(DqcRule po) {
        if (null == po) {
            return new PageUtils<>(new ArrayList<>(),0, po.getPageSize(), po.getPageNo());
        }
        try {
            int totalCount = dqcRuleMapper.count(po);
            if (totalCount <= 0) {
                return new PageUtils<>(new ArrayList<>(), 0, po.getPageSize(), po.getPageNo());
            }
            po.setTotalCount(totalCount);
            //设置总记录数，获取总页数
            //vo.initPage();//分页保护，防止参数传入过大
            //mybatis插件，分页前要先调用下这个语句
            PageHelper.startPage(po.getPageNo(), po.getPageSize(), false);
            List<Map<String, Object>>list = dqcRuleMapper.listTargetTaskIds(po);
            return new PageUtils<>(list, po.getTotalCount(), po.getPageSize(), po.getPageNo());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("mybatis分页查询出错:" + ex);
        }
        return new PageUtils<>(new ArrayList<>(), 0, po.getPageSize(), po.getPageNo());
    }

    public List<Long> listByRelJobIds(List<Long> relJobIds) {
        Assert.collectionNotEmpty(relJobIds, "关联任务id");

        DqcRule dqcRule = new DqcRule();
        dqcRule.setQueryListFieldName("rel_job_id");
        dqcRule.setIsDeleted(false);
        dqcRule.setIds(relJobIds);
        List<DqcRuleVO> vos = this.list(dqcRule);
        if (CollectionUtils.isEmpty(vos)) {
            return Lists.newArrayList();
        }
        return vos.stream().map(DqcRuleVO::getJobId).collect(Collectors.toList());
    }
}
