package com.bigdata.platform.scheduler.web.server.service.basic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.bigdata.platform.scheduler.web.server.service.JobDetailBizService;
import com.bigdata.platform.scheduler.web.server.utils.AESEncryptor;
import com.bigdata.platform.scheduler.common.exception.BizException;
import com.bigdata.platform.scheduler.dal.enums.DbType;
import com.bigdata.platform.scheduler.dal.po.CollectDb;
import com.bigdata.platform.scheduler.dal.po.Job;
import com.bigdata.platform.scheduler.dal.po.JobOnline;
import com.bigdata.platform.scheduler.web.core.service.JobOnlineService;
import com.bigdata.platform.scheduler.web.core.service.JobService;
import com.bigdata.platform.scheduler.web.core.constant.Fields;
import com.bigdata.platform.scheduler.web.server.constant.JsonKey;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class JobUpdateHelper {


    @Autowired
    private JobDetailBizService jobDetailBizService;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobOnlineService jobOnlineService;

    @Autowired
    private AESEncryptor aesEncryptor;


    public String getMainArgs(JSONObject jobJObj) {
        String mainArgs = jobJObj.getString(JsonKey.MAIN_ARGS);
        if (mainArgs == null) {
            return null;
        }
        if (mainArgs.startsWith("'") && mainArgs.endsWith("'")) {
            mainArgs = mainArgs.substring(1, mainArgs.length() - 1);
        }
        return mainArgs;
    }


    /**
     * 不将 mainArgs 转换为 JobCollectDto，避免某些转换参数不一样导致json数据变化
     */
    public String genNewMainArgsForDsUrl(String mainArgs, Long dsId, CollectDb db) throws Exception {
        JSONObject jobCollectDto = null;
        try {
            jobCollectDto = JSONObject.parseObject(mainArgs);
        } catch (Exception e) {
            log.info("mainArgs is not json");
            return null;
        }
        if (jobCollectDto == null) {
            return null;
        }
        JSONObject sourceDb = jobCollectDto.getJSONObject(JsonKey.SOURCE_DB);
        String dsUrl = db.getDsUrl();
        String dbHost = getDbHostFrom(dsUrl);
        String dbName = db.getDbName();
        String dsUser = db.getDsUser();
        String dsPassword = db.getDsPassword();
        DbType dsType = db.getDsType();
        String dbPort = db.getDbPort();
        String encryptPwd = db.getEncryptPwd();
        String pwdKey = db.getPwdKey();

        if (sourceDb != null && dsId.toString().equals(sourceDb.getString(JsonKey.ID))) {
            updateTargetOrSourceDb(sourceDb, dsUrl, dbHost, dbName, dsUser, dsPassword, dsType, dbPort, encryptPwd, pwdKey);
        }
        JSONObject targetDb = jobCollectDto.getJSONObject(JsonKey.TARGET_DB);
        if (targetDb != null && dsId.toString().equals(targetDb.getString(JsonKey.ID))) {
            updateTargetOrSourceDb(targetDb, dsUrl, dbHost, dbName, dsUser, dsPassword, dsType, dbPort, encryptPwd, pwdKey);
        }
        mainArgs = JSONObject.toJSONString(jobCollectDto, SerializerFeature.WriteMapNullValue);
        return mainArgs;
    }

    private void updateTargetOrSourceDb(
            JSONObject dbJObj, String dsUrl, String dbHost, String dbName, String dsUser,
            String dsPassword, DbType dsType, String dbPort, String encryptPwd, String pwdKey) {
        dbJObj.put(JsonKey.DS_URL, dsUrl);
        if (dbHost != null) {
            dbJObj.put(JsonKey.DB_HOST, dbHost);
        }
        dbJObj.put(JsonKey.DB_NAME, dbName);
        dbJObj.put(JsonKey.DS_USER, dsUser);
        // TODO
        dbJObj.put(JsonKey.DS_PASSWORD, dsPassword);
        dbJObj.put(JsonKey.DS_TYPE, dsType.getDbName().toUpperCase());
        dbJObj.put(JsonKey.DB_PORT, dbPort);
        dbJObj.put(JsonKey.ENCRYPT_PWD, encryptPwd);
        dbJObj.put(JsonKey.PWD_KEY, pwdKey);
    }

    private String getDbHostFrom(String dsUrl) {
        String[] arr = dsUrl.split("/");
        if (arr.length >= 3) {
            String[] arr2 = arr[2]
                    .split(":");
            return arr2[0];
        } else {
            return null;
        }
    }


    public Object genNewMainArgsForRunCount(String mainArgs, Long runCount) {
        JSONObject jobCollectDto = JSONObject.parseObject(mainArgs);
        jobCollectDto.put(JsonKey.RUN_COUNT, runCount);
        mainArgs = JSONObject.toJSONString(jobCollectDto, SerializerFeature.WriteMapNullValue);
        return mainArgs;
    }


    public Object genNewMainArgsForMqAddCol(String mainArgs, JSONArray columJArr) {
        JSONObject jobCollectDto = JSONObject.parseObject(mainArgs);
        JSONObject mqDto = jobCollectDto.getJSONObject(JsonKey.MQ_DTO);
        mqDto.put(JsonKey.COLUMNS, columJArr);

        mainArgs = JSONObject.toJSONString(jobCollectDto, SerializerFeature.WriteMapNullValue);
        return mainArgs;
    }


    public void updateNewMainArgs(JSONObject execParamJObj, Long jobId, boolean isJobNotOnline) {
        Map<String, Object> updateParam = Maps.newHashMap();
        updateParam.put(Fields.EXEC_PARAM, execParamJObj.toJSONString());

        if (isJobNotOnline) {
            Job newJob = new Job();
            newJob.setId(jobId);
            newJob.setIsDeleted(false);
            newJob.setUpdateParam(updateParam);
            jobService.edit(newJob);
        } else {
            JobOnline newJobOnline = new JobOnline();
            newJobOnline.setJobId(jobId);
            newJobOnline.setIsDeleted(false);
            newJobOnline.setUpdateParam(updateParam);
            jobOnlineService.edit(newJobOnline);
        }
    }

    /**
     * 解密数据源
     * @param encryptPwd
     * @param pwdKey
     * @return
     */
    private String decrypt(String encryptPwd, String pwdKey) {
        try {
            return this.aesEncryptor.decrypt(encryptPwd,pwdKey);
        } catch (Exception e) {
            throw new BizException("数据源解密失败: " + e.getMessage());
        }
    }


}
