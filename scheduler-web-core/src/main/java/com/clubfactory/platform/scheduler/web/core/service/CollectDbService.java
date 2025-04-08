package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.common.exception.BizException;
import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.scheduler.common.utils.AESUtils;
import com.clubfactory.platform.scheduler.dal.dao.CollectDbMapper;
import com.clubfactory.platform.scheduler.dal.enums.CommonStatus;
import com.clubfactory.platform.scheduler.dal.enums.DbFeatureEnum;
import com.clubfactory.platform.scheduler.dal.enums.DbType;
import com.clubfactory.platform.scheduler.dal.po.CollectDb;
import com.clubfactory.platform.scheduler.web.core.dto.DataSourceDto;
import com.clubfactory.platform.scheduler.web.core.dto.DataSourceEditDto;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.utils.RegexpUtils;
import com.clubfactory.platform.scheduler.web.core.vo.CollectDbVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Service
public class CollectDbService extends BaseNewService<CollectDbVO,CollectDb> {

    @Resource
    CollectDbMapper collectDbMapper;

    private AESUtils aesUtils;

    @PostConstruct
    public void init(){
        setBaseMapper(collectDbMapper);
        this.aesUtils = new AESUtils();
    }



    /**
     * 列出数据源列表
     * @return
     */
    public List<CollectDbVO> listDataSource(DbFeatureEnum feature) {
    	CollectDb collectDb = new CollectDb();
    	collectDb.setIsDeleted(false);
        collectDb.setFeature(feature);
    	collectDb.setStatus(CommonStatus.ENABLED);
    	collectDb.setOrderBy("ds_name asc");
    	return this.list(collectDb);
    }

    public void addNotJdbcSource(DataSourceDto dsDto, Long createUser) {
    	CollectDb collectDb = new CollectDb();
        collectDb.setDsName(dsDto.getDsName());
        collectDb.setDsType(dsDto.getDsType());
        collectDb.setDsUser(dsDto.getDsUser());
        collectDb.setDsUrl(dsDto.getDsUrl());
        collectDb.setFeature(dsDto.getFeature());
        collectDb.setStatus(dsDto.getStatus());
        collectDb.setDbHost(dsDto.getDsUrl());
        collectDb.setDbName(dsDto.getDsName());
        collectDb.setDbHost(dsDto.getDsUrl());
        collectDb.setCreateUser(createUser);
        collectDb.setIsDeleted(false);
        collectDb.setUpdateUser(createUser);

        this.encryptPassword(collectDb, dsDto);
        this.save(collectDb);
    }

    /**
     * 新增数据源
     * @param dsDto
     * @param localUserId
     */
    public void addDataSource(DataSourceDto dsDto, Long localUserId){
        Assert.notNull(dsDto);
        Assert.notNull(localUserId);
        Assert.notNull(dsDto.getDsName(),"数据源名称");

        ensureDSNameNotExists(dsDto.getDsName(),null);
        String dbName = RegexpUtils.extractDbName(dsDto.getDsUrl());
        ensureNotNull(dbName);
        String dbHost = RegexpUtils.extractDbServerHost(dsDto.getDsUrl());
        Assert.notBlank(dbHost,"JDBC URL不合法，未能正确提取DB Server Host信息");
        String dbPort = getDbPort(dsDto.getDsUrl());

        CollectDb collectDb = new CollectDb();
        collectDb.setDbName(dbName);
        collectDb.setDsName(dsDto.getDsName());
        collectDb.setDsType(dsDto.getDsType());
        collectDb.setDsUser(dsDto.getDsUser());
        collectDb.setDsUrl(dsDto.getDsUrl());
        collectDb.setFeature(dsDto.getFeature());
        collectDb.setStatus(dsDto.getStatus());
        collectDb.setCreateUser(localUserId);
        collectDb.setIsDeleted(false);
        collectDb.setUpdateUser(localUserId);
        collectDb.setDbHost(dbHost);
        collectDb.setDbPort(dbPort);

        this.encryptPassword(collectDb, dsDto);

        this.save(collectDb);

    }

    public Map<Long, CollectDb> getCollectDbMap() {
        CollectDb collectDb = new CollectDbVO();
        collectDb.setIsDeleted(false);
        return this.list(collectDb).stream().collect(Collectors.toMap(CollectDb::getId, k->k));
    }

	public void editKafkaSource(DataSourceEditDto editDto, Long updateUser) {
		Assert.notNull(editDto);
		Assert.notNull(updateUser);
		Assert.notNull(editDto.getId());

		CollectDb collectDbRes = this.getById(editDto.getId());
		if (collectDbRes.getDsType() != DbType.KAFKA) {
			throw new BizException(ErrorCode.KAFKA_NOT_ALLOW_ERROR);
		}

		CollectDb collectDb = new CollectDb();
		collectDb.setId(editDto.getId());
		collectDb.setIsDeleted(false);

		Map<String, Object> updateParam = Maps.newHashMap();
		ensureDSNameNotExists(editDto.getDsName(),editDto.getId());
		updateParam.put("ds_name", editDto.getDsName());
		updateParam.put("ds_type", editDto.getDsType());
		updateParam.put("ds_url",editDto.getDsUrl());
		updateParam.put("feature",editDto.getFeature());
		updateParam.put("status",editDto.getStatus());

		collectDb.setUpdateParam(updateParam);
		this.edit(collectDb);
	}

    /**
     * 数据源更新
     * @param editDto
     * @param updateUser
     * @param oldPassword
     * @param isDelete
     */
    public void editDataSource(DataSourceEditDto editDto,Long updateUser, String oldPassword, boolean isDelete){
        Assert.notNull(editDto);
        Assert.notNull(updateUser);
        Assert.notNull(editDto.getId());

        CollectDb collectDb = new CollectDb();
        collectDb.setId(editDto.getId());
        collectDb.setIsDeleted(false);

        Map<String, Object> updateParam = Maps.newHashMap();
        updateParam.put("update_user",updateUser);
        if (StringUtils.isNotEmpty(editDto.getDsName())){
            ensureDSNameNotExists(editDto.getDsName(),editDto.getId());
            updateParam.put("ds_name",editDto.getDsName());
        }
        if (editDto.getDsType() != null ){
            updateParam.put("ds_type",editDto.getDsType().name());
        }
        if (StringUtils.isNotEmpty(editDto.getDsUser())){
            updateParam.put("ds_user",editDto.getDsUser());
        }
        if (StringUtils.isNotEmpty(editDto.getDsPassword())){
            this.encryptPassword(updateParam, editDto, oldPassword);
        }

        if (StringUtils.isNotEmpty(editDto.getDsUrl())){
            String dbName = RegexpUtils.extractDbName(editDto.getDsUrl());
            ensureNotNull(dbName);
            String dbHost = RegexpUtils.extractDbServerHost(editDto.getDsUrl());
            ensureNotNull(dbHost);
            String dbPort = getDbPort(editDto.getDsUrl());
            updateParam.put("ds_url",editDto.getDsUrl());
            updateParam.put("db_name",dbName);
            updateParam.put("db_host",dbHost);
            updateParam.put("db_port",dbPort);
        }
        if (editDto.getFeature() != null){
            updateParam.put("feature",editDto.getFeature());
        }
        if (editDto.getStatus() != null){
            updateParam.put("status",editDto.getStatus());
        }
        if (isDelete){
            updateParam.put("is_deleted",true);
        }

        collectDb.setUpdateParam(updateParam);
        this.edit(collectDb);
    }

    public List<Long> listIdByTypes(List<String> dsTypes) {
    	CollectDb collectDb = new CollectDb();
    	collectDb.setIsDeleted(false);
    	collectDb.setQueryListFieldName("ds_type");
    	collectDb.setIdsString(dsTypes);
    	List<CollectDbVO> vos = this.list(collectDb);
    	if (CollectionUtils.isEmpty(vos)) {
    		return Lists.newArrayList();
    	}
    	return vos.stream().map(CollectDbVO::getId).collect(Collectors.toList());
    }

    private void ensureNotNull(String dbName){
        if (StringUtils.isEmpty(dbName)){
            throw new BizException("JDBC URL不合法或未指定数据库");
        }
    }


    /**
     * 保证数据源名称唯一性
     * @param dsName
     */
    private  void ensureDSNameNotExists(String dsName,Long excludeDsId){
        Long dsId = collectDbMapper.selectIdByDsName(dsName);
        if (dsId != null){
            if (!dsId.equals(excludeDsId)) {
                throw new BizException(String.format("当前已存在名称为【%s】的数据源，请保证数据源名称的唯一性", dsName));
            }
        }
    }

    public List<CollectDbVO> listByDbType(DbType dbType) {
    	CollectDb collectDb = new CollectDb();
        collectDb.setIsDeleted(false);
        collectDb.setDsType(dbType);
        return this.list(collectDb);
    }

    public Map<Long, String> getDsNameMap() {
    	CollectDb collectDb = new CollectDb();
        collectDb.setIsDeleted(false);
        List<CollectDbVO> collectDbs = this.list(collectDb);
        if (CollectionUtils.isEmpty(collectDbs)) {
        	return Maps.newHashMap();
        }
        return collectDbs.stream().collect(Collectors.toMap(CollectDbVO::getId, CollectDbVO::getDsName));
    }

    /**
     * 根据ID获取数据源
     * @param id
     * @return
     */
    public CollectDb getById(Long id) {
        Assert.notNull(id, "dbId");
        CollectDb collectDb = new CollectDb();
        collectDb.setId(id);
        collectDb.setIsDeleted(false);
        return this.get(collectDb);
    }

	public List<CollectDbVO> listByDbTypes(List<String> types) {
		Assert.collectionNotEmpty(types, "类型");
		CollectDb collectDb = new CollectDb();
		collectDb.setQueryListFieldName("ds_type");
		collectDb.setIdsString(types);
		collectDb.setIsDeleted(false);
		collectDb.setStatus(CommonStatus.ENABLED);
		return this.list(collectDb);
	}

    private String getDbPort(String dsUrl){
        String dbPort = RegexpUtils.extractDbServerPort(dsUrl);
        return Optional.ofNullable(dbPort).orElse("-1");
    }

    public Map<String, Long> getMapByName(List<String> names) {
    	CollectDb collectDb = new CollectDb();
    	collectDb.setIsDeleted(false);
    	collectDb.setIdsString(names);
    	collectDb.setQueryListFieldName("ds_name");
    	List<CollectDbVO>  collectDbVOs = this.list(collectDb);
    	return collectDbVOs.stream().collect(Collectors.toMap(CollectDb::getDsName, CollectDb::getId, (oldValue, newValue)-> newValue));
    }

    public CollectDbVO get(String dbHost, String dbName) {
    	CollectDb collectDb = new CollectDb();
    	collectDb.setIsDeleted(false);
    	collectDb.setDbHost(dbHost);
    	collectDb.setDbName(dbName);

    	List<CollectDbVO> vos = this.list(collectDb);
    	if (CollectionUtils.isEmpty(vos)) {
    		return null;
    	}
		return vos.stream().findFirst().get();
    }

    public CollectDbVO getHive(String dbHost) {
    	CollectDb collectDb = new CollectDb();
    	collectDb.setIsDeleted(false);
    	collectDb.setDbHost(dbHost);
    	collectDb.setDbName("default");
    	collectDb.setDsType(DbType.HIVE);
    	List<CollectDbVO> vos = this.list(collectDb);
    	if (CollectionUtils.isEmpty(vos)) {
    		return null;
    	}
		return vos.stream().findFirst().get();
    }

    public List<CollectDbVO> listIds(String dbHost, String dbName, DbType dsType) {
    	Assert.notBlank(dbHost, "dbHost");
    	Assert.notBlank(dbName, "dbName");
    	Assert.notNull(dsType, "dsType");
    	CollectDb collectDb = new CollectDb();
    	collectDb.setIsDeleted(false);
    	collectDb.setDbHost(dbHost);
    	collectDb.setDsType(dsType);
    	if (DbType.HIVE == dsType) {
	    	collectDb.setIdsString(Lists.newArrayList(dbName, "default"));
	    	collectDb.setQueryListFieldName("db_name");
    	} else {
    		collectDb.setDbName(dbName);
    	}
    	collectDb.setStatus(CommonStatus.ENABLED);
    	return this.list(collectDb);
    }

    /**
     * @param dsName
     * @return
     */
    public CollectDb getByDsName(String dsName) {
        Assert.notBlank(dsName, "数据源名称");
        return this.collectDbMapper.selectByDsName(dsName);
    }

    /**
     * 数据源密码加密
     * @param collectDb
     * @param dto
     */
    private void encryptPassword(CollectDb collectDb, DataSourceDto dto) {
        if (dto.getDsPassword() != null) {
            try {
                String pwdKey = this.aesUtils.generateKey();
                String encryptPwd = this.aesUtils.encrypt(dto.getDsPassword(), pwdKey);
                collectDb.setEncryptPwd(encryptPwd);
                collectDb.setPwdKey(pwdKey);
//                collectDb.setDsPassword(dto.getDsPassword());
            } catch (Exception e) {
                throw new BizException("数据源密码加密失败:" + e.getMessage());

            }

        }
    }

    /**
     * 数据源密码加密
     * @param updateParams
     * @param dto
     * @param oldPassword
     */
    private void encryptPassword(Map<String,Object> updateParams, DataSourceEditDto dto, String oldPassword) {
        if (dto.getDsPassword() == null) {
            return;
        }
        try {
            if (!dto.getDsPassword().equals(oldPassword)) {
                String pwdKey = this.aesUtils.generateKey();
                String encryptPwd = this.aesUtils.encrypt(dto.getDsPassword(), pwdKey);
                updateParams.put("encrypt_pwd", encryptPwd);
                updateParams.put("pwd_key", pwdKey);
//                updateParams.put("ds_password", dto.getDsPassword());
            }
        } catch (Exception e) {
            throw new BizException("数据源密码加密失败:" + e.getMessage());

        }
    }
}
