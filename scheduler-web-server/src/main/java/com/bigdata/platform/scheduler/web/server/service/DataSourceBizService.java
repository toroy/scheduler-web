package com.bigdata.platform.scheduler.web.server.service;


import com.bigdata.platform.scheduler.web.server.dto.DataSourceTestDto;
import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.web.server.remote.LogClientManager;
import com.bigdata.platform.scheduler.web.server.tasks.DataSourceConnTestTask;
import com.bigdata.platform.scheduler.web.server.utils.AESEncryptor;
import com.bigdata.platform.scheduler.common.bean.PageUtils;
import com.bigdata.platform.scheduler.common.exception.BizException;
import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.common.util.BeanUtil;
import com.clubfactory.platform.meta.client.dto.TblUpdateDto;
import com.clubfactory.platform.meta.client.service.MetaClientService;
import com.bigdata.platform.scheduler.dal.enums.CommonStatus;
import com.bigdata.platform.scheduler.dal.enums.DbFeatureEnum;
import com.bigdata.platform.scheduler.dal.enums.DbType;
import com.bigdata.platform.scheduler.dal.po.CollectDb;
import com.bigdata.platform.scheduler.dal.po.JobType;
import com.bigdata.platform.scheduler.logger.vo.DBConnVO;
import com.bigdata.platform.scheduler.web.core.dto.DataSourceDto;
import com.bigdata.platform.scheduler.web.core.dto.DataSourceEditDto;
import com.bigdata.platform.scheduler.web.core.enums.ErrorCode;
import com.bigdata.platform.scheduler.web.core.service.CollectDbService;
import com.bigdata.platform.scheduler.web.core.service.MachineService;
import com.bigdata.platform.scheduler.web.core.service.UserService;
import com.bigdata.platform.scheduler.web.core.utils.RegexpUtils;
import com.bigdata.platform.scheduler.web.core.utils.ThreadUtils;
import com.bigdata.platform.scheduler.web.core.vo.CollectDbVO;
import com.bigdata.platform.scheduler.web.server.constant.JsonKey;
import com.bigdata.platform.scheduler.web.server.dto.CipherTextDto;
import com.bigdata.platform.scheduler.web.server.dto.DataSourcePagerDto;
import com.bigdata.platform.scheduler.web.server.service.inter.ConditionCompleter;
import com.bigdata.platform.scheduler.web.server.service.inter.ViewTransformer;
import com.bigdata.platform.scheduler.web.core.utils.JobTypeCache;
import com.bigdata.platform.scheduler.web.server.vo.CipherTextVo;
import com.bigdata.platform.scheduler.web.server.vo.CommonEnumVo;
import com.bigdata.platform.scheduler.web.server.vo.ConnVo;
import com.bigdata.platform.scheduler.web.server.vo.DataSourceVo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoIterable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Slf4j
@Service
public class DataSourceBizService {

    @Autowired
    private CollectDbService collectDbService;

    @Autowired
    private UserService userService;

    @Autowired
    private DataSourceConnTestTask connTestTask;

    @Autowired
    private MachineService machineService;

    @Autowired
    private JobUpdateService jobUpdateService;

    @Autowired
    private AESEncryptor aesEncryptor;

    @Resource
    MetaClientService metaClientService;

    @Autowired
    JobDetailBizService jobDetailBizService;

    /**
     * 数据源测试最大超时时间
     */
    @Value("${db.conn.test.timeout}")
    private Integer connTestTimeout;

    @Value("${task.logger.server.port}")
    private Integer rpcServerPort;

    private LogClientManager logClientManager;


    private ExecutorService executorService;


    @PreDestroy
    public void destroy(){
        if (executorService != null && executorService.isShutdown()){
            executorService.shutdownNow();
        }
    }


    @PostConstruct
    public void init(){
        if (connTestTimeout == null) {
            connTestTimeout = 10;
        }
        if (rpcServerPort == null) {
            rpcServerPort = 50051;
        }
        executorService = ThreadUtils.newDaemonFixedThreadExecutor("db_conn_test_thread",10,20);
        logClientManager = LogClientManager.getInstance();
    }


    /**
     * 分页查询
     * @param pagerDto
     * @return
     */
    public PageUtils<DataSourceVo> queryByPage(DataSourcePagerDto pagerDto){
        ViewTransformer<CollectDb,DataSourceVo> voViewTransformer = po -> {
            DataSourceVo vo = new DataSourceVo();
            if (po != null) {
                BeanUtil.copyBeanNotNull2Bean(po, vo);
                completeVo(po,vo);
            }
            return vo;
        };

        ConditionCompleter<CollectDb,DataSourceVo> conditionCompleter = ds -> {
            List<Long> userIds = userService.listIdsByUserNameAndDepartName(pagerDto.getCreateUser(), null);
            if (CollectionUtils.isNotEmpty(userIds)) {
                ds.setIds(userIds);
                ds.setQueryListFieldName("create_user");
                return null;
            } else {
                return new PageUtils<>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
            }
        };

        return this.queryByPage(pagerDto, conditionCompleter ,voViewTransformer);
    }

    /**
     * 分页查询
     * @return
     */
    private PageUtils<DataSourceVo> queryByPage(DataSourcePagerDto pagerDto,
                                                ConditionCompleter<CollectDb, DataSourceVo> conditionCompleter,
                                                ViewTransformer<CollectDb, DataSourceVo> voViewTransformer){
        Assert.notNull(pagerDto);

        CollectDb ds = new CollectDb();
        ds.setPageNo(pagerDto.getPageNo());
        ds.setPageSize(pagerDto.getPageSize());
        ds.setDsName(pagerDto.getDsName());
        ds.setDsUrl(pagerDto.getDsUrl());
        ds.setIsDeleted(false);
        ds.setOrderBy("update_time desc");

        if (StringUtils.isNotEmpty(pagerDto.getCreateUser())) {
            PageUtils<DataSourceVo>  pageUtils = conditionCompleter.complete(ds);
            if (pageUtils != null){
                return pageUtils;
            }
        }


        PageUtils<CollectDb> pages = collectDbService.pageList(ds);
        if (pages.getSize() == 0) {
            return new PageUtils<>(Lists.newArrayList(), 0, pagerDto.getPageSize(), pagerDto.getPageNo());
        }
        List<DataSourceVo> dataSourceVos = pages.getRows()
                .stream()
                .map(po -> voViewTransformer.transform(po))
                .collect(Collectors.toList());
        return new PageUtils<>(dataSourceVos, pages.getTotalCount(),pages.getPageSize(),pages.getPageNo());
    }

    /**
     * 新增数据源
     * @param dsDto
     * @param createUser
     */
    public void addDataSource(DataSourceDto dsDto, Long createUser){
        Assert.notNull(dsDto);
        dsDto.setDsUrl(trimDsUrl(dsDto.getDsUrl()));

        if (DbType.KAFKA == dsDto.getDsType() || DbType.MONGODB == dsDto.getDsType()) {
            collectDbService.addNotJdbcSource(dsDto, createUser);

        } else {
        	collectDbService.addDataSource(dsDto,createUser);
        }
    }

    /**
     * 更新数据源
     * @param editDto
     * @param updateUser
     */
    public void editDataSource(DataSourceEditDto editDto, LoginUserDto updateUser){
        Assert.notNull(editDto);
        editDto.setDsUrl(trimDsUrl(editDto.getDsUrl()));

        DataSourceTestDto testDto = new DataSourceTestDto();
        testDto.setDsPassword(editDto.getDsPassword());
        testDto.setDsId(editDto.getId());
        testDto.setDsType(editDto.getDsType());
        testDto.setDsUrl(editDto.getDsUrl());
        testDto.setDsUser(editDto.getDsUser());
        //testDataSourceConn(testDto);

        Assert.notNull(editDto);
        Assert.notNull(editDto.getId());
        CollectDb collectDb = new CollectDb();
        collectDb.setId(editDto.getId());
        collectDb.setIsDeleted(false);
        CollectDbVO collectDbVO = collectDbService.get(collectDb);
        if (collectDbVO == null){
            throw new BizException("数据源不存在");
        }
        if (!updateUser.getIsAdmin() && !updateUser.getLocalUserId().equals(collectDbVO.getCreateUser())){
            throw new BizException(ErrorCode.NON_ADMIN_OR_OWNER_ERROR.setParams("更改数据源信息",updateUser.getName()));
        }
        if (editDto.getStatus() == CommonStatus.DISABLED
                && collectDbVO.getStatus() == CommonStatus.ENABLED) {
            Set<Long> jobIds = jobDetailBizService.listOnlineJobIdByDbId(editDto.getId());
            if (CollectionUtils.isNotEmpty(jobIds)) {
                throw new BizException(ErrorCode.DB_CONTANT_JOB_ONLINE_ID.setParams(StringUtils.join(jobIds, ",")));
            }
        }

        if (DbType.KAFKA != editDto.getDsType()) {
            collectDbService.editDataSource(editDto,updateUser.getLocalUserId(),collectDbVO.getEncryptPwd(),false);
            jobUpdateService.updateJobxxTableAndExecParam(null, null,
                    JsonKey.DS_URL, String.valueOf(editDto.getId()));

            String oldDbHost = collectDbVO.getDbHost();
            oldDbHost = oldDbHost == null ? "" : oldDbHost;
            String newDbHost = RegexpUtils.extractDbServerHost(editDto.getDsUrl());
            if (!oldDbHost.equalsIgnoreCase(newDbHost)) {
                metaClientService.updateTableInfo(new TblUpdateDto(oldDbHost, newDbHost));
            }
        }

    }

    @NotNull
    private String trimDsUrl(String dsUrl) {
        if (dsUrl == null) {
            return dsUrl;
        }
        return dsUrl.replaceAll(" ", "");
    }

    /**
     * 删除数据源
     * @param id
     * @param updateUser
     */
    public void delDataSource(Long id, LoginUserDto updateUser){
        Assert.notNull(updateUser);
        Assert.notNull(id);
        CollectDb collectDb = new CollectDb();
        collectDb.setId(id);
        collectDb.setIsDeleted(false);
        CollectDbVO collectDbVO = collectDbService.get(collectDb);
        if (collectDbVO == null){
            throw new BizException("数据源不存在或者已经删除");
        }
        if (!updateUser.getIsAdmin() && !updateUser.getLocalUserId().equals(collectDbVO.getCreateUser())){
            throw new BizException(ErrorCode.NON_ADMIN_OR_OWNER_ERROR.setParams("删除数据源",updateUser.getName()));
        }
        DataSourceEditDto delDto = new DataSourceEditDto();
        delDto.setId(id);
        collectDbService.editDataSource(delDto,updateUser.getLocalUserId(),null,true);
    }


    /**
     * 根据ID查询对应的数据源
     * @param id
     * @return
     */
    public DataSourceVo getDataSourceById(Long id){
        Assert.notNull(id);
        CollectDb po = new CollectDb();
        po.setId(id);
        po.setIsDeleted(false);
        CollectDbVO res =  collectDbService.get(po);
        if (res != null) {
            DataSourceVo vo = new DataSourceVo();
            BeanUtil.copyBeanNotNull2Bean(res, vo);
            completeVo(res,vo);
            return vo;
        }
        return null;
    }

    /**
     * 根据ID查询对应的数据源
     * @param connId
     * @return
     */
    public ConnVo getConnInfoByConnId(String connId){
        Assert.notNull(connId, "connId");
        CollectDb connInfo =  collectDbService.getByDsName(connId);
        if (connInfo != null) {
            ConnVo vo = new ConnVo();
            vo.setId(connInfo.getId());
            vo.setConnId(connId);
            vo.setConnUrl(connInfo.getDsUrl());
            vo.setHost(connInfo.getDbHost());
            vo.setPort(connInfo.getDbPort());
            vo.setEncryptPwd(connInfo.getEncryptPwd());
            vo.setPwdKey(connInfo.getPwdKey());
            vo.setUser(connInfo.getDsUser());
            vo.setDbType(connInfo.getDsType());
            vo.setDbName(connInfo.getDbName());
            return vo;
        }
        return null;
    }

    /**
     * 获取数据源相关的枚举
     * @return
     */
    public Map getEnums(){
        Map<String,List<CommonEnumVo>> response = new HashMap<>(3);
        List<CommonEnumVo> list = Lists.newArrayList();
        for (DbType dbType : DbType.values()){
            list.add(new CommonEnumVo(dbType));
        }
        response.put("type",list);

        list = Lists.newArrayList();
        for (DbFeatureEnum feature : DbFeatureEnum.values()){
            list.add(new CommonEnumVo(feature));
        }
        response.put("feature",list);

        list = Lists.newArrayList();
        for (CommonStatus status : CommonStatus.values()){
            list.add(new CommonEnumVo(status));
        }
        response.put("status",list);
        return response;
    }

    /**
     * 数据源连接测试
     * @param testDto
     * @return
     */
    public Map testDataSourceConn(DataSourceTestDto testDto){
        Assert.notNull(testDto);
        Map status = Maps.newHashMap();

        if (testDto.getDsId() != null) {
            CollectDb collectDb = new CollectDb();
            collectDb.setId(testDto.getDsId());
            collectDb.setIsDeleted(false);
            CollectDbVO collectDbVO = collectDbService.get(collectDb);
            if (collectDbVO != null && testDto.getDsPassword() != null && collectDbVO.getEncryptPwd() != null
                    && testDto.getDsPassword().equals(collectDbVO.getEncryptPwd())) {
                // 密码和库里面完全相同，说明没编辑过，需要解密才能用
                testDto.setDsPassword(this.decrypt(new CipherTextDto(collectDbVO.getEncryptPwd(), collectDbVO.getPwdKey())));
            }
        }

        boolean connStatus;
        if (testDto.getDsType() == DbType.MONGODB) {
            connStatus = testMongodb(testDto);
        } else {
            if (testDto.getDsType() == DbType.MYSQL || testDto.getDsType() == DbType.POSTGRESQL) {
                Map<String, DBConnVO> workerConnStates = testWorkerDataSourceConn(testDto);
                boolean connState = true;
                List<String> connMsg = Lists.newArrayList();
                if (MapUtils.isNotEmpty(workerConnStates)) {
                    for (Map.Entry<String, DBConnVO> entry : workerConnStates.entrySet()) {
                        String workerIp = entry.getKey();
                        DBConnVO dbConnVO = entry.getValue();
                        String errMsg = "连接成功";
                        if (dbConnVO == null) {
                            errMsg = "未知错误";
                        } else if (!dbConnVO.getStatus()) {
                            connState = false;
                            errMsg = dbConnVO.getErrMsg();
                        }
                        if (StringUtils.isNotBlank(workerIp)) {
                            connMsg.add(workerIp + " -> " + errMsg);
                        }
                    }
                    if (!connState) {
                        throw new BizException(String.join("<br/> \n", connMsg));
                    }
                }
            }
            connStatus = testOnWeb(testDto);
        }
        status.put("status",connStatus);
        return status;
    }

    /**
     * Worker端数据库连接测试
     * @param testDto
     * @return
     */
    private Map<String,DBConnVO> testWorkerDataSourceConn(DataSourceTestDto testDto){
        Map<String,DBConnVO> workersState = Maps.newHashMap();
        List<String> workerIpList = machineService.listAllWorkerInfos()
                .stream()
                .filter(po -> {
                    JobType jobType = JobTypeCache.getJobTypeByFunction(po.getFunctions());
                    return jobType != null &&  (jobType.isDataCrawler() || jobType.isDataPush());
                })
                .map(po -> po.getIp().trim())
                .distinct()
                .collect(Collectors.toList());

        long start = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(workerIpList)){
            log.info("开始测试采集回流Worker上{}的连通性",testDto.getDsUrl());
            Map<String,Future<DBConnVO>> futures = Maps.newHashMap();
            for (String workerIp : workerIpList){
                Future<DBConnVO> future = executorService.submit(() -> {
                    try {
                        return logClientManager.callRemote(workerIp,rpcServerPort, logClient ->
                                logClient.testDBConnVO(
                                        testDto.getDsUrl(),testDto.getDsUser(),testDto.getDsPassword(),
                                        testDto.getDsType().name(),connTestTimeout
                                )
                        );
                    }catch (Exception e){
                        log.error("测试失败:{}",e.getMessage());
                        return null;
                    }
                });
                futures.put(workerIp,future);
            }

            boolean waitResult = true;
            Set<String> pollCompleteSet = Sets.newHashSet();
            while (waitResult){
                waitResult = false;
                for (Map.Entry<String,Future<DBConnVO>> entry : futures.entrySet()){
                    try {
                        Future<DBConnVO> future = entry.getValue();
                        if (future.isDone() || future.isCancelled()){
                            String workerIp = entry.getKey();
                            if (!pollCompleteSet.contains(workerIp)) {
                                DBConnVO dbConnVO = future.get();
                                if (dbConnVO != null) {
                                    workersState.put(workerIp, dbConnVO);
                                }
                                pollCompleteSet.add(workerIp);
                            }
                        }else {
                            waitResult = true;
                        }
                    }catch (Exception e){
                        log.error(e.getMessage());
                        waitResult = true;
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }


        }
        return workersState;
    }




    private boolean testOnWeb(DataSourceTestDto testDto){
        DataSourceDto dto = new DataSourceDto();
        BeanUtil.copyBeanNotNull2Bean(testDto,dto);
        Future<Boolean> future = connTestTask.testDatabaseConn(dto, this.connTestTimeout);
        Boolean testRes;
        long startTime = System.currentTimeMillis();
        try {
            testRes = future.get(connTestTimeout, TimeUnit.SECONDS);
            if (testRes == null) {
                throw new BizException("Web端连接超时..");
            }
            return testRes;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Web端connect error: ", e);
            throw new BizException(e.getMessage());
        } catch (TimeoutException e) {
            throw new BizException("Web端连接超时..");
        } finally {
            log.info("测试数据源{}耗时:{} ms", dto.getDsUrl(), System.currentTimeMillis() - startTime);
        }
    }


    public boolean testMongodb(DataSourceTestDto testDto) {
        FutureTask<String> check = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                MongoClient mongoClient = null;
                try {
                    MongoClientURI uri = new MongoClientURI(testDto.getDsUrl());
                    mongoClient = new MongoClient(uri);
                    MongoIterable<String> res = mongoClient.listDatabaseNames();
                    return res.first();
                } catch (Exception e) {
                    log.error("testMongodb 连接失败:{}", e.getMessage());
                    return null;
                } finally {
                    mongoClient.close();
                }
            }
        });
        executorService.submit(check);

        for (int i = 1; i <= 5; i++) {
            try {
                Thread.sleep(1000L);
                if (check.isDone() && check.get() != null) {
                    return true;
                }
            } catch (Exception e) {
                //ignore
            }
        }
        throw new BizException("连接超时..");
    }


    /**
     * 数据源复制
     * @param id
     * @param createUser
     */
    public void copyDataSource(Long id,Long createUser){
        Assert.notNull(id);
        CollectDb collectDb = collectDbService.getById(id);
        Assert.notNull(collectDb);
        DataSourceDto dsDto = new DataSourceDto();
        BeanUtil.copyBeanNotNull2Bean(collectDb,dsDto);
        dsDto.setDsName(String.format("%s_副本",collectDb.getDsName()));
        collectDbService.addDataSource(dsDto,createUser);
    }

    /**
     *
     * @param  po
     * @param  vo
     */
    private void completeVo(CollectDb po, DataSourceVo vo){
        if(po.getCreateUser() != null) {
            vo.setCreateUser(userService.getUserName(po.getCreateUser()));
        }
        vo.setDsPassword(null);
        if (po.getUpdateUser() != null){
            vo.setUpdateUser(userService.getUserName(po.getUpdateUser()));
        }
        if (po.getFeature() != null){
            vo.setFeature(po.getFeature().name());
            vo.setFeatureDesc(po.getFeature().getDesc());
        }
        if (po.getStatus() != null){
            vo.setStatus(po.getStatus().name());
            vo.setStatusDesc(po.getStatus().getDesc());
        }
        if (po.getEncryptPwd() != null) {
            vo.setDsPassword(po.getEncryptPwd());
        }
    }


    /**
     * 加密数据
     * @param message
     * @return
     */
    public CipherTextVo encrypt(String message){
        try {
            String key = aesEncryptor.generateKey();
            CipherTextVo cipherTextVo = new CipherTextVo();
            cipherTextVo.setPwdKey(key);
            cipherTextVo.setCipherText(aesEncryptor.encrypt(message,key));
            return cipherTextVo;
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new BizException(String.format("数据源密码加密失败: %s",e.getMessage()));
        }
    }

    /**
     * 解密数据
     * @param cipherText
     * @return
     */
    public String decrypt(CipherTextDto cipherText){
        try {
            return aesEncryptor.decrypt(cipherText.getCipherText(),cipherText.getPwdKey());
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new BizException(String.format("数据源密码解密失败: %s",e.getMessage()));
        }
    }
}
