package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.scheduler.dal.dao.TableLineageMapper;
import com.clubfactory.platform.scheduler.dal.po.TableLineage;
import com.clubfactory.platform.scheduler.web.core.vo.TableLineageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Service
public class TableLineageService extends BaseNewService<TableLineageVO, TableLineage> {

    @Resource
    TableLineageMapper tableLineageMapper;

    @PostConstruct
    public void init() {
        setBaseMapper(tableLineageMapper);
    }



}
