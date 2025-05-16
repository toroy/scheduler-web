package com.bigdata.platform.scheduler.web.core.dqc.service;

import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.dal.dao.DqcTableMapper;
import com.bigdata.platform.scheduler.dal.po.DqcTable;
import com.bigdata.platform.scheduler.web.core.dqc.vo.DqcTableVO;
import com.bigdata.platform.scheduler.web.core.service.BaseNewService;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class DqcTableService extends BaseNewService<DqcTableVO, DqcTable> {

    @Resource
    DqcTableMapper dqcTableMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(dqcTableMapper);
    }

    public Long getRelJobId(String dbName, String tableName) {
        Assert.notBlank(dbName);
        Assert.notBlank(tableName);

        DqcTable dqcTableDto = new DqcTable();
        dqcTableDto.setDbName(dbName);
        dqcTableDto.setTableName(tableName);
        DqcTable dqcTable = this.get(dqcTableDto);
        if (dqcTable == null) {
            return null;
        }
        return dqcTable.getRelJobId();
    }

    public void update(String dbName, String tableName, Long relJobId, Long userId) {
        Assert.notBlank(dbName);
        Assert.notBlank(tableName);
        Assert.notNull(relJobId);

        DqcTable dqcTableDto = new DqcTable();
        dqcTableDto.setDbName(dbName);
        dqcTableDto.setTableName(tableName);
        Map<String, Object> updateParam = Maps.newHashMap();
        updateParam.put("rel_job_id", relJobId);
        updateParam.put("update_user", userId);
        dqcTableDto.setUpdateParam(updateParam);
        this.edit(dqcTableDto);
    }

    public void save(String dbName, String tableName, Long relJobId, Long userId) {
        Assert.notBlank(dbName);
        Assert.notBlank(tableName);
        Assert.notNull(relJobId);

        DqcTable dqcTableDto = new DqcTable();
        dqcTableDto.setDbName(dbName);
        dqcTableDto.setTableName(tableName);
        dqcTableDto.setCreateUser(userId);
        dqcTableDto.setUpdateUser(userId);
        dqcTableDto.setRelJobId(relJobId);
        this.save(dqcTableDto);
    }

    public void deleteByRelJobIds(List<Long> relJobIds) {
        Assert.collectionNotEmpty(relJobIds, "关联任务Id");

        DqcTable dqcTable = new DqcTable();
        dqcTable.setIds(relJobIds);
        dqcTable.setQueryListFieldName("rel_job_id");
        this.logicRemove(dqcTable);
    }
}
