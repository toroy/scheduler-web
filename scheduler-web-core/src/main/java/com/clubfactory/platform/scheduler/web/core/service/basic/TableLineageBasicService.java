package com.clubfactory.platform.scheduler.web.core.service.basic;


import com.clubfactory.platform.scheduler.dal.dao.TableLineageDependMapper;
import com.clubfactory.platform.scheduler.dal.dao.TableLineageMapper;
import com.clubfactory.platform.scheduler.dal.enums.LineageTypeEnum;
import com.clubfactory.platform.scheduler.dal.po.CollectDb;
import com.clubfactory.platform.scheduler.dal.po.TableLineage;
import com.clubfactory.platform.scheduler.dal.po.TableLineageDepend;
import com.clubfactory.platform.scheduler.web.core.Constants;
import com.clubfactory.platform.scheduler.web.core.service.BaseNewService;
import com.clubfactory.platform.scheduler.web.core.service.ScriptService;
import com.clubfactory.platform.scheduler.web.core.utils.PropertyUtils;
import com.clubfactory.platform.scheduler.web.core.vo.ScriptContentVo;
import com.clubfactory.platform.scheduler.web.core.vo.TableLineageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TableLineageBasicService extends BaseNewService<TableLineageVO,TableLineage> {

    @Resource
    TableLineageMapper tableLineageMapper;
    @Resource
    TableLineageDependMapper tableLineageDependMapper;
    @Resource
    ScriptService scriptService;

    private List<String> allowViewList;

    @PostConstruct
    public void init(){
        setBaseMapper(tableLineageMapper);
    }

    public String getScriptContent(Long scriptId) {
        if (allowViewList == null) {
            String viewSuffixes = PropertyUtils.getString(Constants.RESOURCE_VIEW_SUFFIXES);
            String[] viewArr = StringUtils.split(viewSuffixes, ",");
            if (viewArr != null) {
                allowViewList = Arrays.asList(viewArr);
            }else {
                allowViewList = new ArrayList<>();
            }
        }
        ScriptContentVo scriptContentVo = scriptService.getScriptContentById(scriptId, null, allowViewList);
        return scriptContentVo.getContent();
    }


    public void delAllBy(Long jobId) {
        TableLineage tl = new TableLineageVO();
        tl.setJobId(jobId);
        //List<TableLineage> tls = tableLineageMapper.list(tl);
        //List<Long> ids = tls.stream().map(ele -> ele.getId()).collect(Collectors.toList());
        tableLineageMapper.remove(tl);

        TableLineageDepend tld = new TableLineageDepend();
        tld.setJobId(jobId);
        tableLineageDependMapper.remove(tld);
    }


    public Long createTableLineage(Long jobId, String tableName,
                                   LineageTypeEnum lte, Long userId, CollectDb db) {
        String[] arr = tableName.split("[.]");
        String dbName = null;
        if (arr.length == 2) {
            dbName = arr[0];
            tableName = arr[1];
        }
        TableLineage tl = new TableLineage();
        tl.setJobId(jobId);
        tl.setTableName(tableName);
        tl.setCreateUser(userId);
        tl.setUpdateUser(userId);
        tl.setType(lte);

        if (db != null) {
            tl.setDbHost(db.getDbHost());
            tl.setDbName(dbName == null ? db.getDbName() : dbName);
            tl.setDbType(db.getDsType());
        }
        return save(tl).getId();
    }


}
