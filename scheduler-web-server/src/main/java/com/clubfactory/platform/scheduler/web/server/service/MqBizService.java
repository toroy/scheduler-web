package com.clubfactory.platform.scheduler.web.server.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.clubfactory.platform.common.bean.BaseResult;
import com.clubfactory.platform.common.bean.PageUtils;
import com.clubfactory.platform.common.exception.BizException;
import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.enums.*;
import com.clubfactory.platform.scheduler.dal.po.CollectDb;
import com.clubfactory.platform.scheduler.dal.po.Job;
import com.clubfactory.platform.scheduler.dal.po.Mq;
import com.clubfactory.platform.scheduler.web.core.constant.SysConfigConstant;
import com.clubfactory.platform.scheduler.web.core.dto.AlarmDto;
import com.clubfactory.platform.scheduler.web.core.dto.JobCollectDto;
import com.clubfactory.platform.scheduler.web.core.dto.JobColumnDto;
import com.clubfactory.platform.scheduler.web.core.dto.JobDto.ParamContent;
import com.clubfactory.platform.scheduler.web.core.dto.MqDto;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.jdbc.client.HiveOperations;
import com.clubfactory.platform.scheduler.web.core.jdbc.client.Operations;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.enums.HiveColumnTypeEnum;
import com.clubfactory.platform.scheduler.web.core.service.*;
import com.clubfactory.platform.scheduler.web.core.utils.OkHttp3Utils;
import com.clubfactory.platform.scheduler.web.core.utils.SpringBean;
import com.clubfactory.platform.scheduler.web.core.utils.SysConfigUtil;
import com.clubfactory.platform.scheduler.web.core.vo.CollectDbVO;
import com.clubfactory.platform.scheduler.web.core.vo.MqVO;
import com.clubfactory.platform.scheduler.web.server.constant.UpdateJobEnums;
import com.clubfactory.platform.scheduler.web.server.dto.MqQueryDto;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.utils.AESEncryptor;
import com.clubfactory.platform.scheduler.web.server.vo.JobEnumVo.Content;
import com.clubfactory.platform.scheduler.web.server.vo.MqEnumVo;
import com.clubfactory.platform.scheduler.web.server.vo.MqQueryVo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Maps;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MqBizService {

	@Resource
	MqService mqService;
	@Resource
	UserService userService;
	@Resource
	ColumnService columnService;
	@Resource
	CollectDbService collectDbService;
	@Resource
	JobDetailBizService jobDetailBizService;
	@Resource
	JobBizService jobBizService;
	@Resource
	JobService jobService;
	@Resource
	JobCollectService jobCollectService;
	@Resource
	JobUpdateService jobUpdateService;
	@Resource
	UserInfoBizService userInfoBizService;

	@Resource
	AESEncryptor aesEncryptor;
	
	private static final String SPARK_STREAMING = "SPARK_STREAMING";
	private static final String DEFAULT_DB = "ods_kafka";
	
	public MqEnumVo listEnums() {
		MqEnumVo mqEnum = new MqEnumVo();
		
		List<HiveColumnTypeEnum> columnTypeEnums = Lists.newArrayList();
		columnTypeEnums.add(HiveColumnTypeEnum.STRING);
		columnTypeEnums.add(HiveColumnTypeEnum.TIMESTAMP);
		columnTypeEnums.add(HiveColumnTypeEnum.INT);
		columnTypeEnums.add(HiveColumnTypeEnum.BIGINT);
		columnTypeEnums.add(HiveColumnTypeEnum.DOUBLE);
		columnTypeEnums.add(HiveColumnTypeEnum.DATE);
		columnTypeEnums.add(HiveColumnTypeEnum.BOOLEAN);

		List<Content> columnTypes = columnTypeEnums.stream().map(hiveEnum -> {
			Content content = new Content();
			content.setDesc(hiveEnum.name());
			content.setValue(hiveEnum.name());
			return content;
		}).collect(Collectors.toList());
		mqEnum.setColumnTypes(columnTypes);
		
		List<CollectDbVO> collectDbVOs = collectDbService.listByDbType(DbType.KAFKA);
		if (CollectionUtils.isEmpty(collectDbVOs)) {
			return mqEnum;
		}
		List<Content> dbs = collectDbVOs.stream().map(collect -> {
			Content content = new Content();
			content.setDesc(collect.getDsName());
			content.setValue(collect.getId());
			return content;
		}).collect(Collectors.toList());
		mqEnum.setDbs(dbs);
		
		mqEnum.setDataTypes(listEnums(MqDataTypeEnum.values()));
		mqEnum.setOffsetTypes(listEnums(MqOffsetTypeEnum.values()));
		return mqEnum;
	}
	
	private List<Content> listEnums(IEnum[] values) {
		return Lists.newArrayList(values).stream().map(typeEnum -> {
			Content content = new Content();
			content.setValue(typeEnum.name());
			content.setDesc(typeEnum.getDesc());
			return content;
		}).collect(Collectors.toList());
	}
	
	@Transactional
	public Boolean save(MqDto mqDto, LoginUserDto userDto) {
		Assert.notNull(mqDto);
		Assert.notBlank(mqDto.getTopicName());
		Assert.notBlank(mqDto.getTableName());
		Assert.collectionNotEmpty(mqDto.getColumns(), "字段列表");
		checkTableName(mqDto.getTableName());
		
		List<ColumnDto> newColumnDtos = listNewColumns(mqDto.getColumns());
		Assert.collectionNotEmpty(newColumnDtos, "字段列表");
		
		// 建hive表
		if (BooleanUtils.isTrue(mqDto.getIsCreateTable())) {
			String sql = genCreateTableSql(mqDto, newColumnDtos);
			this.executeHive(mqDto, sql);
		}
		
		// kafka建topic
		if (BooleanUtils.isTrue(mqDto.getIsCreateTopic())) {
			this.genTopic(mqDto, userDto.getToken());
		}
		
		// 数据库插入数据
		Long id = mqService.saveMqDto(mqDto, userDto.getLocalUserId());
		columnService.saveBatchColumn(mqDto.getColumns(), id, userDto.getLocalUserId());
		return true;
	}

	private void genTopic(MqDto mqDto, String token) {
		CollectDb collectDb = collectDbService.getById(mqDto.getDbId());
		String createUrlReq = String.format(SysConfigUtil.getByKey(SysConfigConstant.MQ_ADD_TOPIC_URL), mqDto.getTopicName(), getUrl(collectDb), mqDto.getTopicDesc(), mqDto.getTopicPartition());
		try {
			Map<String, String> headMap = Maps.newHashMap();
			headMap.put("Cookie", "SSO_AUTH_TOKEN="+token);
			Response response = OkHttp3Utils.get(createUrlReq, headMap);
			if (response == null) {
				log.error(String.format("createUrlReq:%s, headMap:%s", createUrlReq, JSON.toJSONString(headMap)));
				throw new BizException(ErrorCode.CREATE_TOPIC_ERROR.setParams("返回为null"));
			}
			
			if (response.code() != 200) {
				throw new BizException(ErrorCode.CREATE_TOPIC_ERROR.setParams(response.message()));
			}
			
			String msg = response.body().string();
			BaseResult<String> baseResult = JSON.parseObject(msg, BaseResult.class);
			if (baseResult.getCode() == 0 || baseResult.getCode() == 200) {
				return;
			}
			throw new BizException(ErrorCode.CREATE_TOPIC_ERROR.setParams(baseResult.getMessage()));
			
		} catch (IOException e) {
			throw new BizException(ErrorCode.CREATE_TOPIC_ERROR.setParams(e.getMessage()));
		}
	}

	private String getUrl(CollectDb collectDb) {
		try {
			return URLEncoder.encode(collectDb.getParam(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void executeHive(MqDto mqDto, String sql) {
		Long dbId = getHiveDbId();
		CollectDb db = collectDbService.getById(dbId);
		DbDto dbDto = new DbDto();
		dbDto.setDbType(db.getDsType());
		String dsUrl = db.getDsUrl();
		dbDto.setDbUrl(dsUrl);
		dbDto.setDbUser(db.getDsUser());
		dbDto.setDbPwd(this.decrypt(db.getEncryptPwd(),db.getPwdKey()));
		Operations operations = SpringBean.getBean(HiveOperations.class);
		try {
			operations.init(dbDto, dsUrl);
			operations.execute(sql, dsUrl);
			operations.close(dsUrl);
		} catch (Exception e) {
			throw new BizException(ErrorCode.EXECUTE_HIVE_SQL_ERROR.setParams(e.getMessage()));
		}
		
	}

	private Long getHiveDbId() {
		Long dbId = SysConfigUtil.getLongByKey(SysConfigConstant.MQ_HIVE_DB_ID);
		if (dbId == null) {
			throw new BizException(ErrorCode.MQ_HIVE_DB_NOT_SET);
		}
		return dbId;
	}
	
	// 获取库.表名
	private String getHiveTableName(String tableName) {
		if (StringUtils.contains(tableName, ".")) {
			return tableName;
		} else {
			return String.format("%s.%s", DEFAULT_DB, tableName);
		}
	}
	
	private void checkTableName(String tableName) {
		String[] nameArr = StringUtils.split(tableName, ".");
		if (!StringUtils.equals(DEFAULT_DB, nameArr[0])) {
			throw new BizException(ErrorCode.MQ_DB_NOT_SUPPORT.setParams(DEFAULT_DB));
		}
	}
	
	public String genCreateTableSql(MqDto mqDto,List<ColumnDto> newColumnDtos) {
		StringBuilder sb = new StringBuilder();
		sb.append(" create table IF NOT EXISTS ");
		sb.append(getHiveTableName(mqDto.getTableName()));
		sb.append(" ( ");
		List<String> columnLists = genColumn(newColumnDtos);
		// 额外增加的字段
		columnLists.add(0," ext_data String comment '全部数据'");
		
		sb.append(StringUtils.join(columnLists, ","));
		sb.append(" ) "); 
		if (StringUtils.isNotBlank(mqDto.getTopicDesc())) {
			sb.append(" comment ");
			sb.append(" ' ");
			sb.append(mqDto.getTopicDesc());
			sb.append(" ' ");
		}
		sb.append(" partitioned by (pt String comment '分区字段') ");
		sb.append(" stored as ORCFILE ");
		return sb.toString();
	}
	
	private List<ColumnDto> listNewColumns(List<ColumnDto> columns) {
		return columns.stream().filter(column -> BooleanUtils.isTrue(column.getIsNew())).collect(Collectors.toList());
	}
	
	private List<ColumnDto> listOldColumns(List<ColumnDto> columns) {
		return columns.stream().filter(column -> BooleanUtils.isNotTrue(column.getIsNew())).collect(Collectors.toList());
	}

	private List<String> genColumn(List<ColumnDto> newColumnDtos) {
		List<String> columnLists = newColumnDtos.stream().map(columnDto -> {
			StringBuilder columnSb = new StringBuilder();
			columnSb.append("`");
			columnSb.append(columnDto.getName());
			columnSb.append("`");
			columnSb.append(" ");
			columnSb.append(columnDto.getType());
			columnSb.append(" comment ");
			columnSb.append(" ' ");
			columnSb.append(columnDto.getDesc());
			columnSb.append(" ' ");
			return columnSb.toString();
		}).collect(Collectors.toList());
		return columnLists;
	}
	
	public String genAddColumnSql(MqDto mqDto, List<ColumnDto> newColumnDtos) {
		StringBuilder sb = new StringBuilder();
		sb.append("  alter table ");
		sb.append(mqDto.getTableName());
		sb.append("  add columns ");
		sb.append(" ( ");
		List<String> columnLists = genColumn(newColumnDtos);
		sb.append(StringUtils.join(columnLists, ","));
		sb.append(" ) "); 
		return sb.toString();
	}
	
	@Transactional
	public Boolean edit(MqDto mqDto, LoginUserDto userDto) {
		Assert.notNull(mqDto);
		Assert.notBlank(mqDto.getTableName());
		checkTableName(mqDto.getTableName());
		
		Mq mq = mqService.getById(mqDto.getId(), userDto.getLocalUserId(), userDto.getIsAdmin());
		if (mq == null) {
			throw new BizException(ErrorCode.MQ_NOT_PERMISSION);
		}
		if (StringUtils.isBlank(mqDto.getTopicName())) {
			mqDto.setTopicName(mq.getTopicName());
		}
		// 改接入元数据信息
		mqService.editMqDto(mqDto, userDto.getLocalUserId());

		// hive增加字段
		List<ColumnDto> newColumnDtos = listNewColumns(mqDto.getColumns());
		if (CollectionUtils.isNotEmpty(newColumnDtos)) {
			String sql = genAddColumnSql(mqDto, newColumnDtos);
			this.executeHive(mqDto, sql);
			// 增加字段信息
			columnService.saveBatchColumn(newColumnDtos, mqDto.getId(), userDto.getLocalUserId());
		}

		List<ColumnDto> oldColumnDtos = listOldColumns(mqDto.getColumns());
		if (CollectionUtils.isNotEmpty(oldColumnDtos)) {
			// 改表字段描述
			for (ColumnDto dto : oldColumnDtos) {
				columnService.editColumnDesc(mqDto.getId(), dto.getName(), dto.getDesc());
			}
		}

		// 改采集表的信息
		if (mq.getJobId() != null) {
			jobCollectService.editSourceByJobId(mq.getJobId(), mqDto.getTopicName(), mqDto.getTableName(), mqDto.getDbId());
		}

		// 重新审核
		if (mq.getJobId() != null) {
			jobBizService.editCheck(mq.getJobId());
		}

		JSONArray allColumJArr = (JSONArray) JSONArray.toJSON(mqDto.getColumns());
		jobUpdateService.updateJobxxTableAndExecParam(mq.getJobId(), null,
				UpdateJobEnums.MQ_COLUMNS.getDesc(), allColumJArr.toJSONString());
		return true;
	}
	
	public MqDto get(Long id, LoginUserDto userDto) {
		Assert.notNull(id);
		Mq mq = mqService.getById(id);
		if (mq == null) {
			throw new BizException(ErrorCode.MQ_NOT_EXISTS);
		}
		MqDto mqDto = new MqDto();
		BeanUtil.copyBeanNotNull2Bean(mq, mqDto);
		
		List<ColumnDto> columnDtos = columnService.listByForeignId(id);
		mqDto.setColumns(columnDtos);
		
		CollectDb db = collectDbService.getById(mq.getDbId());
		mqDto.setDsName(db.getDsName());
		
		// 是否可以编辑的按钮
		mqDto.setIsEdit(false);
		if (userDto.getIsAdmin() || userDto.getLocalUserId().equals(mq.getCreateUser())) {
			mqDto.setIsEdit(true);
		}
		return mqDto;
	}
	
	public MqDto getById(Long id) {
		Assert.notNull(id);
		Mq mq = mqService.getById(id);
		if (mq == null) {
			throw new BizException(ErrorCode.MQ_NOT_EXISTS);
		}
		return getMqDto(mq);
	}
	
	public MqDto getByJobId(Long jobId) {
		Assert.notNull(jobId);
		Mq mq = mqService.getByJobId(jobId);
		if (mq == null) {
			throw new BizException(ErrorCode.MQ_NOT_EXISTS);
		}
		return getMqDto(mq);
	}

	private MqDto getMqDto(Mq mq) {
		MqDto mqDto = new MqDto();
		BeanUtil.copyBeanNotNull2Bean(mq, mqDto);
		
		mqDto.setKafkaSaslMechanism(SysConfigUtil.getByKey(SysConfigConstant.kafkaSaslMechanism));
		mqDto.setKafkaSecurityProtocol(SysConfigUtil.getByKey(SysConfigConstant.kafkaSecurityProtocol));
//		mqDto.setKafkaSyncMergeHivePartiPeriodMinutes(SysConfigUtil.getNumberByKey(SysConfigConstant.kafkaSyncMergeHivePartiPeriodMinutes));
//		mqDto.setKafkaSyncWaterMarkerCurMinuteDecrX(SysConfigUtil.getNumberByKey(SysConfigConstant.kafkaSyncWaterMarkerCurMinuteDecrX));
//		mqDto.setKafkaSyncWriteFsPeriodMinutes(SysConfigUtil.getNumberByKey(SysConfigConstant.kafkaSyncWriteFsPeriodMinutes));
//		mqDto.setKafkaSyncWriteHivePeriodMinutes(SysConfigUtil.getNumberByKey(SysConfigConstant.kafkaSyncWriteHivePeriodMinutes));
		
		List<ColumnDto> columnDtos = columnService.listByForeignId(mq.getId());
		mqDto.setColumns(columnDtos);
	
		return mqDto;
	}
	
	public String getTopicName() {
		return "topic_gaia_"+new Date().getTime();
	}
	
	

	public PageUtils<MqQueryVo> listByPage(MqQueryDto queryDto, LoginUserDto userDto) {
		Assert.notNull(queryDto);
		Assert.notNull(queryDto.getPageNo());
		Assert.notNull(queryDto.getPageSize());

		MqVO mqVO = new MqVO();
		if (StringUtils.isNotBlank(queryDto.getUserName())) {
			List<Long> userIds = userService.listIdsByUserNameAndDepartName(queryDto.getUserName(), null);
			if (CollectionUtils.isEmpty(userIds)) {
				return new PageUtils<MqQueryVo>(Lists.newArrayList(), 0, queryDto.getPageSize(), queryDto.getPageNo());
			}
			mqVO.setIds(userIds);
			mqVO.setQueryListFieldName("create_user");
		}
		mqVO.setTopicName(queryDto.getTopicName());
		mqVO.setTopicDesc(queryDto.getTopicDesc());
		mqVO.setOrderBy("update_time desc");
		mqVO.setPageNo(queryDto.getPageNo());
		mqVO.setPageSize(queryDto.getPageSize());

		PageUtils<MqVO> pageVo = mqService.pageVoList(mqVO);

		// 返回数据
		if (pageVo.getSize() == 0) {
			return new PageUtils<MqQueryVo>(queryDto.getPageSize(), queryDto.getPageNo());
		}

		Map<Long, String> dsNameMap = collectDbService.getDsNameMap();
		List<MqVO> rows = pageVo.getRows();
		
		List<Long> jobIds = listJobIds(rows);
		List<MqQueryVo> queryVos = rows.stream().map(vo -> {
			MqQueryVo queryVo = new MqQueryVo();
			BeanUtil.copyBeanNotNull2Bean(vo, queryVo);
			queryVo.setUserName(userService.getUserName(vo.getCreateUser()));
			queryVo.setDsName(dsNameMap.get(vo.getDbId()));
			queryVo.setIsGenJob(false);
			if (jobIds.contains(vo.getJobId())) {
				queryVo.setIsGenJob(true);	
			}
			
			return queryVo;
		}).collect(Collectors.toList());
		
		return new PageUtils<MqQueryVo>(queryVos, pageVo.getTotalCount(), queryDto.getPageSize(), queryDto.getPageNo());
	}

	private List<Long> listJobIds(List<MqVO> rows) {
		List<Long> jobIds = rows.stream().filter(mq -> mq.getJobId() != null).map(MqVO::getJobId).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(jobIds)) {
			return jobService.listByIds(jobIds).stream().map(Job::getId).collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}
	
	public Boolean genJob(Long id, LoginUserDto userDto) {
		Assert.notNull(id);
		Mq mq = mqService.getById(id, userDto.getLocalUserId(), userDto.getIsAdmin());
		if (mq == null) {
			throw new BizException(ErrorCode.MQ_NOT_PERMISSION);
		}
		if (mq.getJobId() != null) {
			Job job = jobService.getById(mq.getJobId());
			if (job != null) {
				throw new BizException(ErrorCode.GEN_JOB_REPEAT_ERROR.setParams(mq.getJobId().toString()));
			}
		}
		
		Long jobId = genJobCollect(userDto, mq);
		if (jobId != null) {
			mqService.editJobIdById(jobId, id);
		}
		return true;
	}

	private Long genJobCollect(LoginUserDto userDto, Mq mq) {
		Long dbId = getHiveDbId();
		
		JobCollectDto jobDto = new JobCollectDto();
		jobDto.setMqId(mq.getId());
		
		jobDto.setCategroy(JobCategoryEnum.COLLECT);
		jobDto.setScriptId(SysConfigUtil.getLongByKey(SysConfigConstant.MQ_SCRIPT_ID));
		jobDto.setMachineId(0L);
		jobDto.setName(mq.getTopicName());
		jobDto.setTargetTable(mq.getTableName());
		jobDto.setSourceTable(mq.getTopicName());
		jobDto.setRunCount(1L);
		jobDto.setPriority(PriorityEnum.HIGH.getCode());
		jobDto.setDbTargetId(dbId);
		jobDto.setDbSourceId(mq.getDbId());
		jobDto.setCycleType(JobCycleTypeEnum.REAL_TIME);
		jobDto.setStorageFormat(FormatEnum.ORC);
		jobDto.setType(SPARK_STREAMING);
		jobDto.setSysParams(Lists.newArrayList());
		jobDto.setMainClass(SysConfigUtil.getByKey(SysConfigConstant.MQ_JOB_MAIN_CLASS));
		jobDto.setDeployMode(DeployModeEnum.CLUSTER);
		jobDto.setProgramType(ProgramTypeEnum.JAVA);
		
		ParamContent content = new ParamContent();
		content.setName("--queue streaming");
		jobDto.setSysParams(Lists.newArrayList(content));
		
		// 重试设置
		jobDto.setRetryDur(SysConfigUtil.getNumberByKey(SysConfigConstant.MQ_JOB_RETRY_DUR));
		jobDto.setRetryMax(SysConfigUtil.getNumberByKey(SysConfigConstant.MQ_JOB_RETRY_COUNT));
		
		// 告警设置
		AlarmDto alarm = new AlarmDto();
		alarm.setTypes(Sets.newHashSet(AlarmTypeEnum.FAILED));
		alarm.setNoticeType(AlarmNoticeTypeEnum.EMAIL);
		alarm.setUserGroupIds(Sets.newHashSet(userInfoBizService.getLoginUserDefaultGroupId()));
		jobDto.setAlarms(Arrays.asList(alarm));

		jobDto.setColumnDto(new JobColumnDto());
		return jobDetailBizService.saveCollect(jobDto,userDto);
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
	
	public static void main(String[] args) {
		
		//String topic = "topic_gaia_"+new Date().getTime() + StringUtils.UUID.randomUUID().toString();
		//System.out.println(topic);
	}


}
