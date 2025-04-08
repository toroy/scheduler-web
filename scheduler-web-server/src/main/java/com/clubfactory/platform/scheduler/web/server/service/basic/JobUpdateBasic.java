package com.clubfactory.platform.scheduler.web.server.service.basic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.clubfactory.platform.scheduler.dal.po.CollectDb;
import com.clubfactory.platform.scheduler.dal.po.Job;
import com.clubfactory.platform.scheduler.web.core.vo.JobCalVO;
import com.clubfactory.platform.scheduler.web.core.vo.JobCollectVO;
import com.clubfactory.platform.scheduler.web.core.vo.JobReflueVO;
import com.clubfactory.platform.scheduler.web.server.constant.JsonKey;
import com.clubfactory.platform.scheduler.web.server.service.JobDetailBizService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class JobUpdateBasic {

    @Autowired
    private JobDetailBizService jobDetailBizService;
    @Autowired
    private JobUpdateHelper jobUpdateHelper;


    public void updateExecParamForDsUrl(String execParam, Long jobId, Long dsId,
                                        CollectDb collectDb, boolean isJobNotOnline) throws Exception {
        JSONObject execParamJObj = JSONObject.parseObject(execParam);
        String mainArgs = jobUpdateHelper.getMainArgs(execParamJObj);
        if (mainArgs == null) {
            log.info("mainArgs is null, jobId:" + jobId + ", isJobNotOnline:" + isJobNotOnline);
            return;
        }
        String newMainArgs = jobUpdateHelper.genNewMainArgsForDsUrl(mainArgs, dsId, collectDb);
        if (newMainArgs == null) {
            return;
        }
        execParamJObj.put(JsonKey.MAIN_ARGS, newMainArgs);

        jobUpdateHelper.updateNewMainArgs(execParamJObj, jobId, isJobNotOnline);
    }


    public void updateExecParamForRunCount(String execParam, Long jobId, Long runCount, boolean isJobNotOnline) {
        JSONObject execParamJObj = JSONObject.parseObject(execParam);
        String mainArgs = jobUpdateHelper.getMainArgs(execParamJObj);

        execParamJObj.put(JsonKey.MAIN_ARGS, jobUpdateHelper.genNewMainArgsForRunCount(mainArgs, runCount));

        jobUpdateHelper.updateNewMainArgs(execParamJObj, jobId, isJobNotOnline);
    }


    public void fillCollJobIdsSet(List<JobCollectVO> jobCollList, Set<Long> jobIdsSet) {
        for (JobCollectVO vo : jobCollList) {
            jobIdsSet.add(vo.getJobId());
        }
    }


    public void fillCalJobIdsSet(List<JobCalVO> jobCalList, Set<Long> jobIdsSet) {
        for (JobCalVO vo : jobCalList) {
            jobIdsSet.add(vo.getJobId());
        }
    }


    public void fillReflueJobIdsSet(List<JobReflueVO> jobReflueList, Set<Long> jobIdsSet) {
        for (JobReflueVO vo : jobReflueList) {
            jobIdsSet.add(vo.getJobId());
        }
    }

    public void updateExecParamForMqAddCol(Job job, JSONArray columJArr) {
        JSONObject execParamJObj = JSONObject.parseObject(job.getExecParam());
        String mainArgs = jobUpdateHelper.getMainArgs(execParamJObj);

        execParamJObj.put(JsonKey.MAIN_ARGS, jobUpdateHelper.genNewMainArgsForMqAddCol(mainArgs, columJArr));

        jobUpdateHelper.updateNewMainArgs(execParamJObj, job.getId(), true);
    }
}
