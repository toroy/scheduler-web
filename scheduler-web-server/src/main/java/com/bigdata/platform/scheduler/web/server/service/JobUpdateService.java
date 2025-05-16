package com.bigdata.platform.scheduler.web.server.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.bigdata.platform.scheduler.dal.po.CollectDb;
import com.bigdata.platform.scheduler.dal.po.Job;
import com.bigdata.platform.scheduler.dal.po.JobCal;
import com.bigdata.platform.scheduler.dal.po.JobCollect;
import com.bigdata.platform.scheduler.dal.po.JobOnline;
import com.bigdata.platform.scheduler.dal.po.JobReflue;
import com.bigdata.platform.scheduler.dal.enums.DbType;
import com.bigdata.platform.scheduler.web.core.constant.Fields;
import com.bigdata.platform.scheduler.web.core.service.CollectDbService;
import com.bigdata.platform.scheduler.web.core.service.JobCalService;
import com.bigdata.platform.scheduler.web.core.service.JobCollectService;
import com.bigdata.platform.scheduler.web.core.service.JobOnlineService;
import com.bigdata.platform.scheduler.web.core.service.JobReflueService;
import com.bigdata.platform.scheduler.web.core.service.JobService;
import com.bigdata.platform.scheduler.web.core.vo.JobCalVO;
import com.bigdata.platform.scheduler.web.core.vo.JobCollectVO;
import com.bigdata.platform.scheduler.web.core.vo.JobReflueVO;
import com.bigdata.platform.scheduler.web.server.constant.UpdateJobEnums;
import com.bigdata.platform.scheduler.web.server.service.basic.JobUpdateBasic;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobUpdateService {

    @Autowired
    private CollectDbService collectDbService;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobCalService jobCalService;
    @Autowired
    private JobCollectService jobCollectService;
    @Autowired
    private JobReflueService jobReflueService;
    @Autowired
    private JobOnlineService jobOnlineService;
    @Autowired
    private JobUpdateBasic jobUpdateBasic;

    public void syncJobExecParam() {
    	List<String> dsTypes = Lists.newArrayList();
    	dsTypes.add(DbType.MYSQL.name());
    	dsTypes.add(DbType.POSTGRESQL.name());
    	dsTypes.add(DbType.MONGODB.name());
    	List<Long> ids = collectDbService.listIdByTypes(dsTypes);
    	if (CollectionUtils.isEmpty(ids)) {
    		return;
    	}
    	String idsString = StringUtils.join(ids, ",");
    	loopDsId(idsString);
    }
    

    @Transactional
    public void updateJobxxTableAndExecParam(
            Long targetId, String targetTable, String fieldName, String fieldVals) {
        try {
            if (UpdateJobEnums.DS_URL.getDesc().equalsIgnoreCase(fieldName)) {
                loopDsId(fieldVals);

            } else if (UpdateJobEnums.RUN_COUNT.getDesc().equalsIgnoreCase(fieldName)) {
                updateRunCount(targetId, targetTable, fieldVals);

            } else if (UpdateJobEnums.MQ_COLUMNS.getDesc().equalsIgnoreCase(fieldName)) {
                updateMqColumns(targetId, fieldVals);

            }
        } catch (Exception e) {
            log.error("", e);
        }
    }


    private void loopDsId(String fieldVals) {
        String[] dsIdArr = fieldVals.split(",");
        for (String str : dsIdArr) {
            try {
                Long dsId = Long.parseLong(str);

                if (dsId == null || dsId < 0) {
                    log.warn("dsId 必须 >= 0");
                    return;
                }
                CollectDb collectDb = collectDbService.getById(dsId);
                if (collectDb == null) {
                    log.warn("dsId：" + dsId + " 对应的 CollectDb 不存在");
                    return;
                }
                String dsUrl = collectDb.getDsUrl();

                Set<Long> jobIdsSet = new HashSet<>();
                JobCollect jobColl1 = new JobCollect();
                jobColl1.setDbSourceId(dsId);
                jobColl1.setIsDeleted(false);
                List<JobCollectVO> jobCollList1 = jobCollectService.list(jobColl1);
                jobUpdateBasic.fillCollJobIdsSet(jobCollList1, jobIdsSet);

                JobCollect jobColl2 = new JobCollect();
                jobColl2.setDbTargetId(dsId);
                jobColl2.setIsDeleted(false);
                List<JobCollectVO> jobCollList2 = jobCollectService.list(jobColl2);
                jobUpdateBasic.fillCollJobIdsSet(jobCollList2, jobIdsSet);

                JobCal jobCal = new JobCal();
                jobCal.setDbTargetId(dsId);
                jobCal.setIsDeleted(false);
                List<JobCalVO> jobCalList = jobCalService.list(jobCal);
                jobUpdateBasic.fillCalJobIdsSet(jobCalList, jobIdsSet);

                JobReflue jobReflue1 = new JobReflue();
                jobReflue1.setDbSourceId(dsId);
                jobReflue1.setIsDeleted(false);
                List<JobReflueVO> jobReflueList1 = jobReflueService.list(jobReflue1);
                jobUpdateBasic.fillReflueJobIdsSet(jobReflueList1, jobIdsSet);

                JobReflue jobReflue2 = new JobReflue();
                jobReflue2.setDbTargetId(dsId);
                jobReflue2.setIsDeleted(false);
                List<JobReflueVO> jobReflueList2 = jobReflueService.list(jobReflue2);
                jobUpdateBasic.fillReflueJobIdsSet(jobReflueList2, jobIdsSet);

                List<Long> jobIdsList = new ArrayList<>(jobIdsSet);
                List<Job> jobs = jobService.listByIds(jobIdsList);
                for (Job job : jobs) {
                    try {
                        jobUpdateBasic.updateExecParamForDsUrl(
                                job.getExecParam(), job.getId(), dsId, collectDb, true);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }

                List<JobOnline> jobOnlines = jobOnlineService.listByIds(jobIdsList);
                for (JobOnline jobOnline : jobOnlines) {
                    try {
                        jobUpdateBasic.updateExecParamForDsUrl(
                                jobOnline.getExecParam(), jobOnline.getJobId(), dsId, collectDb, false);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }


    private void updateRunCount(Long dbTargetId, String targetTable, String runCountStr) {
        JobCollect jobColl = new JobCollect();
        jobColl.setDbTargetId(dbTargetId);
        jobColl.setTargetTable(targetTable);
        jobColl.setIsDeleted(false);
        List<JobCollectVO> vos = jobCollectService.list(jobColl);

        Set<Long> jobIds = new HashSet<>();
        vos.stream().forEach(vo -> jobIds.add(vo.getJobId()));
        Long runCount = Long.parseLong(runCountStr);

        for (Long jobId : jobIds) {
            JobCollect jobCollToUpdate = new JobCollect();
            jobCollToUpdate.setJobId(jobId);

            Map<String, Object> updateParam = Maps.newHashMap();
            updateParam.put(Fields.RUN_COUNT, runCount);

            jobCollToUpdate.setUpdateParam(updateParam);
            jobCollectService.edit(jobCollToUpdate);

            JobReflue jobReflueToUpdate = new JobReflue();
            jobReflueToUpdate.setJobId(jobId);
            jobReflueToUpdate.setUpdateParam(updateParam);
            jobReflueService.edit(jobReflueToUpdate);
        }

        List<Long> jobIdsList = new ArrayList<>(jobIds);
        List<Job> jobs = jobService.listByIds(jobIdsList);
        for (Job job : jobs) {
            jobUpdateBasic.updateExecParamForRunCount(
                    job.getExecParam(), job.getId(), runCount, true);
        }

        List<JobOnline> jobOnlines = jobOnlineService.listByIds(jobIdsList);
        for (JobOnline jobOnline : jobOnlines) {
            jobUpdateBasic.updateExecParamForRunCount(
                    jobOnline.getExecParam(), jobOnline.getJobId(), runCount, false);
        }
    }


    private void updateMqColumns(Long jobId, String jarrStr) {
        JSONArray columJArr = JSONArray.parseArray(jarrStr);

        Job job = jobService.getById(jobId);
        jobUpdateBasic.updateExecParamForMqAddCol(job, columJArr);
    }


}
