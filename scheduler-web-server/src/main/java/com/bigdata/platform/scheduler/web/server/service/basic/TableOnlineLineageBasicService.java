package com.bigdata.platform.scheduler.web.server.service.basic;

import com.bigdata.platform.scheduler.common.bean.BaseResult;
import com.bigdata.platform.scheduler.dal.dao.TableOnlineLineageMapper;
import com.bigdata.platform.scheduler.dal.enums.LineageTypeEnum;
import com.bigdata.platform.scheduler.dal.po.TableOnlineLineage;
import com.bigdata.platform.scheduler.dal.po.TableOnlineLineageDepend;
import com.bigdata.platform.scheduler.web.client.vo.TableLineageGraphVo;
import com.bigdata.platform.scheduler.web.core.service.BaseNewService;
import com.bigdata.platform.scheduler.web.core.service.TableOnlineLineageDependService;
import com.bigdata.platform.scheduler.web.core.vo.TableOnlineLineageVO;
import com.bigdata.platform.scheduler.web.core.constant.Fields;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class TableOnlineLineageBasicService extends BaseNewService<TableOnlineLineageVO, TableOnlineLineage> {


    @Resource
    TableOnlineLineageMapper tableOnlineLineageMapper;
    @Resource
    TableOnlineLineageDependService tableOnlineLineageDependService;

    @PostConstruct
    public void init(){
        setBaseMapper(tableOnlineLineageMapper);
    }


    public BaseResult<TableLineageGraphVo> getTableLineageGraph(String dbName, String dbHost, String tableName) {
        TableOnlineLineage tol = new TableOnlineLineage();
        tol.setDbName(dbName);
        tol.setDbHost(dbHost);
        tol.setTableName(tableName);
        tol.setIsDeleted(false);
        List<TableOnlineLineage> tols = tableOnlineLineageMapper.list(tol);
        TableLineageGraphVo rootTlgv = new TableLineageGraphVo();
        if (CollectionUtils.isEmpty(tols)) {
            return new BaseResult<>(rootTlgv);
        }
        List<Long> asParentIds = new ArrayList<>();
        List<Long> asChildIds = new ArrayList<>();

        for (TableOnlineLineage ele : tols) {
            if (ele.getType() == LineageTypeEnum.PARENT) {
                asParentIds.add(ele.getLineageId());
            } else if (ele.getType() == LineageTypeEnum.CHILD) {
                asChildIds.add(ele.getLineageId());
            }
        }

        TableOnlineLineage firstTol = tols.get(0);
        rootTlgv.setId(firstTol.getId());
        rootTlgv.setDbName(firstTol.getDbName());
        rootTlgv.setDbHost(firstTol.getDbHost());
        rootTlgv.setTableName(firstTol.getTableName());
        rootTlgv.setParents(getParentsBy(asChildIds));
        rootTlgv.setChilds(getChildsBy(asParentIds));

        return new BaseResult<>(rootTlgv);
    }


    private List<TableLineageGraphVo> getParentsBy(List<Long> asChildIds) {
        List<TableOnlineLineageDepend> tolds = tableOnlineLineageDependService
                .listPoByField(Fields.LINEAGE_ID, asChildIds);
        List<Long> parentIds = tolds.stream().map(told -> told.getParentId()).collect(Collectors.toList());
        List<TableOnlineLineage> parentTols = this.listPoByField(Fields.LINEAGE_ID, parentIds);

        return getTableLineageGraphVosFrom(parentTols);
    }

    private List<TableLineageGraphVo> getTableLineageGraphVosFrom(List<TableOnlineLineage> tols) {
        return tols.stream().map(ele -> {
            TableLineageGraphVo tlgv = new TableLineageGraphVo();
            tlgv.setId(ele.getId());
            tlgv.setDbName(ele.getDbName());
            tlgv.setDbHost(ele.getDbHost());
            tlgv.setTableName(ele.getTableName());
            return tlgv;
        }).collect(Collectors.toList());
    }

    private List<TableLineageGraphVo> getChildsBy(List<Long> asParentIds) {
        List<TableOnlineLineageDepend> tolds = tableOnlineLineageDependService
                .listPoByField(Fields.PARENT_ID, asParentIds);
        List<Long> childIds = tolds.stream().map(told -> told.getLineageId()).collect(Collectors.toList());
        List<TableOnlineLineage> childTols = this.listPoByField(Fields.LINEAGE_ID, childIds);

        return getTableLineageGraphVosFrom(childTols);
    }


    public BaseResult<TableLineageGraphVo> getTableLineageGraph(Long tableId) {
        TableOnlineLineage tol = new TableOnlineLineage();
        tol.setId(tableId);
        TableOnlineLineage po = get(tol);
        return getTableLineageGraph(po.getDbName(), po.getDbHost(), po.getTableName());
    }


}
