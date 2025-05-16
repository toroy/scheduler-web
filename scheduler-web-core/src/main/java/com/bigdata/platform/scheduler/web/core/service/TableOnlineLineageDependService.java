package com.bigdata.platform.scheduler.web.core.service;

import com.bigdata.platform.scheduler.common.util.BeanUtil;
import com.bigdata.platform.scheduler.dal.dao.TableOnlineLineageDependMapper;
import com.bigdata.platform.scheduler.dal.po.TableLineageDepend;
import com.bigdata.platform.scheduler.dal.po.TableOnlineLineageDepend;
import com.bigdata.platform.scheduler.web.core.constant.Fields;
import com.bigdata.platform.scheduler.web.core.vo.TableOnlineLineageDependVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class TableOnlineLineageDependService extends BaseNewService<TableOnlineLineageDependVO, TableOnlineLineageDepend> {

    @Resource
    TableOnlineLineageDependMapper tableOnlineLineageDependMapper;

    @PostConstruct
    public void init() {
        setBaseMapper(tableOnlineLineageDependMapper);
    }


    public void physicsRemoveByJobIds(List<Long> jobIds) {
        TableOnlineLineageDepend told = new TableOnlineLineageDepend();
        told.setQueryListFieldName(Fields.JOB_ID);
        told.setIds(jobIds);
        tableOnlineLineageDependMapper.remove(told);
    }

    public void saveBatchFrom(List<TableLineageDepend> tlds) {
        List<TableOnlineLineageDepend> tolds = new ArrayList<>();
        for (TableLineageDepend tld : tlds) {
            TableOnlineLineageDepend told = new TableOnlineLineageDepend();
            BeanUtil.copyBeanNotNull2Bean(tld, told);
            told.setId(null);
            told.setCreateUser(tld.getCreateUser());
            told.setUpdateUser(tld.getUpdateUser());
            tolds.add(told);
        }
        if (CollectionUtils.isNotEmpty(tolds)) {
            tableOnlineLineageDependMapper.saveBatch(tolds);
        }
    }


}
