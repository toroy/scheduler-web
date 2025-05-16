package com.bigdata.platform.scheduler.web.core.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bigdata.platform.scheduler.dal.dao.TableLineageDependMapper;
import com.bigdata.platform.scheduler.dal.po.TableLineageDepend;
import com.bigdata.platform.scheduler.web.core.vo.TableLineageDependVO;

import java.util.List;

@Service
public class TableLineageDependService extends BaseNewService<TableLineageDependVO,TableLineageDepend> {

    @Resource
    TableLineageDependMapper tableLineageDependMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(tableLineageDependMapper);
    }

    public void createDepends(Long childTlId, List<Long> parentIds, Long userId, Long jobId) {
        for (Long parentId : parentIds) {
            TableLineageDepend tld = new TableLineageDependVO();
            tld.setLineageId(childTlId);
            tld.setParentId(parentId);
            tld.setJobId(jobId);
            tld.setCreateUser(userId);
            tld.setUpdateUser(userId);
            save(tld);
        }
    }


}
