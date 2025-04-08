package com.clubfactory.platform.scheduler.web.server.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.clubfactory.platform.common.bean.tuple.Tuple2;
import com.clubfactory.platform.common.exception.BizException;
import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.common.ParamConstants;
import com.clubfactory.platform.scheduler.dal.enums.*;
import com.clubfactory.platform.scheduler.dal.po.*;
import com.clubfactory.platform.scheduler.web.core.constant.SysConfigConstant;
import com.clubfactory.platform.scheduler.web.core.dto.*;
import com.clubfactory.platform.scheduler.web.core.dto.JobDto.ParamContent;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.jdbc.client.*;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.clubfactory.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.clubfactory.platform.scheduler.web.core.service.*;
import com.clubfactory.platform.scheduler.web.core.utils.JobTypeCache;
import com.clubfactory.platform.scheduler.web.core.utils.SpringBean;
import com.clubfactory.platform.scheduler.web.core.utils.SysConfigUtil;
import com.clubfactory.platform.scheduler.web.core.vo.GroupInfoVO;
import com.clubfactory.platform.scheduler.web.core.vo.JobOnlineVO;
import com.clubfactory.platform.scheduler.web.core.vo.JobVO;
import com.clubfactory.platform.scheduler.web.server.dqc.service.DqcRuleBizService;
import com.clubfactory.platform.scheduler.web.server.dto.ColumnQueryDto;
import com.clubfactory.platform.scheduler.web.server.dto.ExecParamDto;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.service.inter.JobEditCallback;
import com.clubfactory.platform.scheduler.web.server.service.inter.JobSaveCallback;
import com.clubfactory.platform.scheduler.web.server.utils.AESEncryptor;
import com.clubfactory.platform.scheduler.web.server.vo.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobDetailBizService {
	
	@Resource
	JobCollectService jobCollectService;
	@Resource
	JobReflueService jobReflueService;
	@Resource
	CollectDbService collectDbService;
	@Resource
	AlarmService alarmService;
	@Resource
	JobService jobService;
	@Resource
	JobCalService jobCalService;
	@Resource
	ScriptService scriptService;
	@Resource
	TaskService taskService;
	@Resource
	MqBizService mqBizService;
	@Resource
	JobOnlineService jobOnlineService;
	@Resource
	ParamService paramService;
	@Resource
	JobDependsService jobDependsService;
	@Resource
	ProjectService projectService;
	@Resource
	TableLineageService tableLineageService;
	@Resource
	UserInfoBizService userInfoBizService;
	@Resource
	DqcRuleBizService dqcRuleBizService;

	@Resource
	AESEncryptor aesEncryptor;
	
	private final static String VERTICAL_LINE = "|";
	
	private final static String DOT = ".";
	
	public Long save(JobDto jobDto, LoginUserDto userDto, JobSaveCallback callback) {
		// 任务名检查
		checkName(jobDto);
		// 报警校验配置
		alarmService.isValidAlarm(jobDto.getAlarms());
		// 表示任务类型
		getJobType(jobDto);
		// 任务保存
		Long id = jobService.save(jobDto, userDto.getLocalUserId(), userDto.getDepartName());
		callback.doInSaveDetail(userDto.getLocalUserId(), id);
		// 报警配置
		alarmService.save(jobDto.getAlarms(), jobDto.getId(), userDto.getLocalUserId());
		return id;
	}

	private void getJobType(JobDto jobDto) {
		if (jobDto.getJobType() == null) {
			jobDto.setJobType(JobTypeEnum.NORMAL);
		}
	}
	
	@Transactional
	public Long saveCal(JobCalDto jobDto, LoginUserDto userDto) {
		Assert.notNull(jobDto);
		jobDto.setCategroy(JobCategoryEnum.CAL);
		
		if (JobTypeCache.isStream(jobDto.getCategroy(), jobDto.getType())) {
			jobDto.setDeployMode(DeployModeEnum.CLUSTER);
		}
		// 名字去除首尾空格
		handleNameTrim(jobDto);
		addDefaultParam(jobDto);
		// 分离系统参数
		handleSystemConfig(jobDto);
		// 生成执行参数
		this.genExecParam(jobDto);
		// 不限制超时
		jobDto.setTimeOut(SysConfigUtil.getNumberByKey(SysConfigConstant.JOB_CAL_TIME_OUT));
		Long jobId = this.save(jobDto, userDto, new JobSaveCallback() {
			@Override
			public void doInSaveDetail(Long localUserId, Long id) {
				// 计算内容
				jobCalService.save(jobDto, id, userDto.getLocalUserId());
			}
			
		});
		return jobId;
	}

	private void handleNameTrim(JobDto jobDto) {
		if (StringUtils.isNotBlank(jobDto.getName())) {
			jobDto.setName(jobDto.getName().trim());
		}
		if (jobDto instanceof JobCalDto) {
			JobCalDto jobCalDto = (JobCalDto) jobDto;
			if (StringUtils.isNotBlank(jobCalDto.getTargetTable())) {
				jobCalDto.setTargetTable(jobCalDto.getTargetTable().trim());
			}
		}

		if (jobDto instanceof JobCollectDto) {
			JobCollectDto jobCollectDto = (JobCollectDto) jobDto;
			if (StringUtils.isNotBlank(jobCollectDto.getSourceTable())) {
				jobCollectDto.setSourceTable(jobCollectDto.getSourceTable().trim());
			}
			if (StringUtils.isNotBlank(jobCollectDto.getTargetTable())) {
				jobCollectDto.setTargetTable(jobCollectDto.getTargetTable().trim());
			}
		}

		if (jobDto instanceof JobReflueDto) {
			JobReflueDto jobReflueDto = (JobReflueDto) jobDto;
			if (StringUtils.isNotBlank(jobReflueDto.getSourceTable())) {
				jobReflueDto.setSourceTable(jobReflueDto.getSourceTable().trim());
			}
			if (StringUtils.isNotBlank(jobReflueDto.getTargetTable())) {
				jobReflueDto.setTargetTable(jobReflueDto.getTargetTable().trim());
			}
		}

	}

	/**
	 * 增加默认参数
	 * @param jobDto
	 */
	private void addDefaultParam(JobDto jobDto) {
		if (!StringUtils.equalsAnyIgnoreCase(jobDto.getType(), "SPARK")) {
			return;
		}
		boolean isExist = false;
		final List<ParamContent> paramContents = Optional.ofNullable(jobDto.getSysParams()).orElse(Lists.newArrayList());
		for (ParamContent paramContent : paramContents) {
			if (paramContent.getName().contains("sparkOffline") && paramContent.getName().contains("--queue")) {
				isExist = true;
				break;
			}
		}
		if (!isExist) {
			ParamContent paramContent = new ParamContent();
			paramContent.setName("--queue sparkOffline");
			paramContents.add(paramContent);
			jobDto.setSysParams(paramContents);
		}
	}

	// 根据是否系统用到的信息分离系统参数，到jobConf里
	private void handleSystemConfig(JobDto jobDto) {
		if (CollectionUtils.isEmpty(jobDto.getSysParams())) {
			return;
		}
		List<String> keys = paramService.listSystemKey();
		if (CollectionUtils.isEmpty(keys)) {
			return;
		}
		
		Map<String, Object> map = Maps.newHashMap();
		Iterator<ParamContent> iterator = jobDto.getSysParams().iterator();
		while(iterator.hasNext()) {
			ParamContent content = iterator.next();
			if (StringUtils.isBlank(content.getName())) {
				continue;
			}
			if (keys.contains(content.getName())) {
				map.put(content.getName(), content.getValue());
				iterator.remove();
			}
		}
		jobDto.setJobConf(JSON.toJSONString(map));
		
	}
	
	
	@Transactional
	public Long saveCollect(JobCollectDto jobDto, LoginUserDto userDto) {
		Assert.notNull(jobDto);
		jobDto.setCategroy(JobCategoryEnum.COLLECT);
		// 检查任务目标表是否重复
		checkTargetTable(jobDto.getDbTargetId(), jobDto.getTargetTable(), jobDto.getId());
		// 名字去除收尾空格
		handleNameTrim(jobDto);
		// 分离系统参数
		handleSystemConfig(jobDto);
		// 生成执行参数
		this.genCollectExecParam(jobDto);
		// 超时限制1小时
		jobDto.setTimeOut(SysConfigUtil.getNumberByKey(SysConfigConstant.JOB_COLLECT_TIME_OUT));
		Long jobId = this.save(jobDto, userDto, new JobSaveCallback() {
			@Override
			public void doInSaveDetail(Long localUserId, Long id) {
				// 采集内容
				jobCollectService.save(jobDto, id, userDto.getLocalUserId());
			}
		});
		// 加/减自依赖
		addOrDelSelfDepnedsByIncrementType(jobDto, userDto.getLocalUserId(), jobId);
		return jobId;
	}
	
	// 检查目标表是否重复，重复报错
	private void checkTargetTable(Long targetId, String targetTable, Long jobId) {
		Tuple2<Boolean, Long> isExist = jobCollectService.isExistTargetTable(targetId, targetTable, jobId);
		if (BooleanUtils.isTrue(isExist.f0)) {
			throw new BizException(ErrorCode.JOB_COLLECT_TARGET_TABLE_REPEAT.setParams(
					Optional.ofNullable(targetId).orElse(0L).toString()
					, targetTable, isExist.f1.toString()));
		}
	}
	
	@Transactional
	public Long saveReflue(JobReflueDto jobDto, LoginUserDto userDto) {
		Assert.notNull(jobDto);
		jobDto.setCategroy(JobCategoryEnum.REFLUE);
		// 名字去除收尾空格
		handleNameTrim(jobDto);
		// 生成执行参数
		this.genReflueExecParam(jobDto);
		// 分离系统参数
		handleSystemConfig(jobDto);
		// 生成执行参数
		this.genReflueExecParam(jobDto);
		// 超时限制1小时
		jobDto.setTimeOut(SysConfigUtil.getNumberByKey(SysConfigConstant.JOB_REFLUE_TIME_OUT));
		Long jobId = this.save(jobDto, userDto, new JobSaveCallback() {
			@Override
			public void doInSaveDetail(Long localUserId, Long id) {
				// 同步内容
				jobReflueService.save(jobDto, id, userDto.getLocalUserId());
			}
			
		});
		return jobId;
	}
	
	@Transactional
	public Boolean editCollect(JobCollectDto jobDto, LoginUserDto userDto) {
		Assert.notNull(jobDto);
		jobDto.setCategroy(JobCategoryEnum.COLLECT);
		// 检查任务目标表是否重复
		checkTargetTable(jobDto.getDbTargetId(), jobDto.getTargetTable(), jobDto.getId());
		// 名字去除收尾空格
		handleNameTrim(jobDto);
		// 分离系统参数
		handleSystemConfig(jobDto);
		this.genCollectExecParam(jobDto);
		edit(jobDto, userDto, new JobEditCallback() {
			@Override
			public void doInEditDetail(Long localUserId, Boolean isAdmin) {
				jobCollectService.edit(jobDto, userDto.getLocalUserId(), userDto.getIsAdmin());
			}
			
		});
		// 加/减自依赖
		addOrDelSelfDepnedsByIncrementType(jobDto, userDto.getLocalUserId(), jobDto.getId());
		return true;
	} 
	
	private void addOrDelSelfDepnedsByIncrementType(JobCollectDto jobDto, Long userId, Long jobId) {
		if (jobDto.getColumnDto() == null || jobDto.getColumnDto().getIncrementType() == null) {
			return;
		}
		if (IncrementTypeEnum.ADD == jobDto.getColumnDto().getIncrementType()) {
			Boolean isExist = jobDependsService.isExistSelfDepends(jobId);
			if (BooleanUtils.isFalse(isExist)) {
				jobDependsService.addSelfDepends(jobId, userId);
			}
		} if (IncrementTypeEnum.ALL == jobDto.getColumnDto().getIncrementType()) {
			jobDependsService.removeSelfDepends(jobId);
		}
	}

	@Transactional
	public Boolean editReflue(JobReflueDto jobDto, LoginUserDto userDto) {
		Assert.notNull(jobDto);
		Assert.notNull(jobDto);
		jobDto.setCategroy(JobCategoryEnum.REFLUE);
		// 名字去除收尾空格
		handleNameTrim(jobDto);
		// 分离系统参数
		handleSystemConfig(jobDto);
		this.genReflueExecParam(jobDto);
		
		edit(jobDto, userDto, new JobEditCallback() {
			@Override
			public void doInEditDetail(Long localUserId, Boolean isAdmin) {
				jobReflueService.edit(jobDto, userDto.getLocalUserId(), userDto.getIsAdmin());
			}
			
		});
		return true;
	} 
	
	@Transactional
	public Boolean editCal(JobCalDto jobDto, LoginUserDto userDto) {
		Assert.notNull(jobDto);
		jobDto.setCategroy(JobCategoryEnum.CAL);
		// 名字去除首尾空格
		handleNameTrim(jobDto);
		addDefaultParam(jobDto);
		this.genExecParam(jobDto);
		// 分离系统参数
		handleSystemConfig(jobDto);
		// 获取执行参数
		this.genExecParam(jobDto);
		edit(jobDto, userDto, new JobEditCallback() {
			@Override
			public void doInEditDetail(Long localUserId, Boolean isAdmin) {
				jobCalService.edit(jobDto, userDto.getLocalUserId(), userDto.getIsAdmin());
			}
			
		});
		return true;
	}
	
	public Boolean edit(JobDto jobDto, LoginUserDto userDto, JobEditCallback jobEditCallback) {
		// 检查是否重新审核
		Boolean isCheck = isCheck(jobDto, userDto);
		jobEditCallback.doInEditDetail(userDto.getLocalUserId(), userDto.getIsAdmin());
		// 更改重试次数和间隔
		editInit(jobDto.getId(), jobDto.getRetryDur(), jobDto.getRetryMax());
		// 任务名检查
		checkName(jobDto);
		// 报警配置检验
		alarmService.isValidAlarm(jobDto.getAlarms());
		// 任务保存
		jobService.edit(jobDto, userDto.getLocalUserId(), userDto.getIsAdmin(), isCheck);
		// 报警配置
		alarmService.edit(jobDto.getAlarms(), jobDto.getId(), userDto.getLocalUserId());
		dqcRuleBizService.editAlarm(jobDto.getId(), userDto);;
		return true;
	}

	private void checkName(JobDto jobDto) {
		Boolean isExist = jobService.isExistName(jobDto.getName(), jobDto.getId());
		if (BooleanUtils.isTrue(isExist)) {
			throw new BizException(ErrorCode.JOB_NAME_REPEAT_ERROR.setParams(jobDto.getName()));
		}
	}


	private void editInit(Long id, Integer retryDur, Integer retryMax) {
		Assert.notNull(id);
		Assert.notNull(retryDur);
		Assert.notNull(retryMax);
		Job job = jobService.getById(id);
		if (job == null) {
			return;
		}
		if (!job.getRetryDur().equals(retryDur) || !job.getRetryMax().equals(retryMax)) {
			taskService.editRetryByJobId(id, retryMax, retryDur);
			jobOnlineService.editRetryByJobId(id, retryMax, retryDur);
		}
	}

	private Boolean isCheck(JobDto jobDto, LoginUserDto userDto) {
		Boolean isCheck = true;
		JobDto resDtp = this.get(jobDto.getId(), userDto);
		if (StringUtils.equals(resDtp.toString(), jobDto.toString())) {
			isCheck = false;
		}
		return isCheck;
	} 

	private void genExecParam(JobDto jobDto) {
		ExecParamDto execParamDto = new ExecParamDto();
		if (jobDto.getCategroy() == JobCategoryEnum.CAL) {
			execParamDto.setMainArgs(jobDto.getArgsParam());
		} else {
			// 采集，回流 需要数据库配置信息，所有将整个对象塞给脚本，自己取
			String systemArgs = JSON.toJSONString(jobDto, SerializerFeature.WriteMapNullValue);
			execParamDto.setMainArgs(systemArgs);
		}
		
		execParamDto.setLanguageType(jobDto.getProgramType());
		execParamDto.setMainClass(jobDto.getMainClass());
		if (jobDto.getDeployMode() != null) {
			execParamDto.setDeployMode(jobDto.getDeployMode().name().toLowerCase());
		}
		execParamDto.setSysConfigs(StringUtils.join(listSysConfigs(jobDto), ParamConstants.GAIA_JOB_PARAM_DELIMITED));
		jobDto.setExecParam(JSON.toJSONString(execParamDto));
	}

	private List<String> listSysConfigs(JobDto jobDto) {
		List<String> contents = Lists.newArrayList();
		if (CollectionUtils.isEmpty(jobDto.getSysParams())) {
			return contents;
		}
		for (ParamContent content : jobDto.getSysParams()) {
			if (StringUtils.isBlank(content.getName())) {
				continue;
			}
			StringBuilder sb = new StringBuilder();
			sb.append(content.getName());
			if (StringUtils.isNotBlank(content.getValue())) {
				sb.append("=");
				sb.append(content.getValue());
			}
			contents.add(sb.toString());
		}
		return contents;
	}
	
	public void genReflueExecParam(JobReflueDto jobDto) {
		CollectDb targetDb = collectDbService.getById(jobDto.getDbTargetId());
		CollectDb sourceDb = collectDbService.getById(jobDto.getDbSourceId());
		
		jobDto.setTargetDb(targetDb);
		jobDto.setSourceDb(sourceDb);
		
		this.genExecParam(jobDto);
	}

	private void genCollectExecParam(JobCollectDto jobDto) {
		CollectDb targetDb = collectDbService.getById(jobDto.getDbTargetId());
		CollectDb sourceDb = collectDbService.getById(jobDto.getDbSourceId());
		jobDto.setTargetDb(targetDb);
		jobDto.setSourceDb(sourceDb);
		
		if (JobTypeCache.isStream(jobDto.getCategroy(), jobDto.getType())) {
			jobDto.setMainClass(SysConfigUtil.getByKey(SysConfigConstant.MQ_JOB_MAIN_CLASS));
			jobDto.setProgramType(ProgramTypeEnum.JAVA);
			
			generatePwd(jobDto);
			// mq的信息
			genMqDto(jobDto);
		} 
		
		this.genExecParam(jobDto);
	}

	private void generatePwd(JobCollectDto jobDto) {
		CollectDb sourceDb = jobDto.getSourceDb();
		if (sourceDb != null) {
			sourceDb.setDsPassword(this.decrypt(sourceDb.getEncryptPwd(),sourceDb.getPwdKey()));
		}
		CollectDb targetDb = jobDto.getTargetDb();
		if (targetDb != null) {
			targetDb.setDsPassword(this.decrypt(targetDb.getEncryptPwd(),targetDb.getPwdKey()));
		}
	}

	private void genMqDto(JobCollectDto jobDto) {
		MqDto mqDto = null;
		if (jobDto.getMqId() != null) {
			mqDto = mqBizService.getById(jobDto.getMqId());
		} else if (jobDto.getId() != null) {
			mqDto = mqBizService.getByJobId(jobDto.getId());
		}
		jobDto.setMqDto(mqDto);
	}
	
	public JobOnlineVo getOnline(Long id, LoginUserDto userDto) {
		Assert.notNull(id);
		JobOnlineVo jobDto = new JobOnlineVo();
		// 查任务信息
		JobOnline jobOnline = jobOnlineService.getById(id);
		if (jobOnline == null) {
			return jobDto;
		}
		this.getJobOnlineDto(jobOnline, jobDto);
		
		// 报警信息
		List<AlarmDto> alarmVo = getAlarmVo(id);
		jobDto.setAlarms(alarmVo);

		// 合并jobConfig 和 param
		mergeJobConfigAndParam(jobDto);
		
		// 获取脚本名称
		jobDto.setScriptName(scriptService.getName(jobOnline.getScriptId()));
		
		// 增加字段信息
		genJobColumn(jobOnline, jobDto);
		
		jobDto.setExecParam(null);
		return jobDto;
	}

	private void genJobColumn(JobOnline jobOnline, JobOnlineDto dto) {
		JobColumnDto jobColumnDto = new JobColumnDto();
		BeanUtil.copyBeanNotNull2Bean(jobOnline, jobColumnDto);
		jobColumnDto.setTargetColumns(string2Lists(jobOnline.getTargetColumns()));
		jobColumnDto.setSourceColumns(string2Lists(jobOnline.getSourceColumns()));
		jobColumnDto.setWhere(jobOnline.getWhereSql());
		dto.setColumnDto(jobColumnDto);
	}

	public Set<Long> listOnlineJobIdByDbId(Long dbId) {
		Assert.notNull(dbId);

		Set<Long> ids = Sets.newHashSet();
		JobOnline job = new JobOnline();
		job.setDbTargetId(dbId);
		job.setIsDeleted(false);
		List<JobOnlineVO> jobOnlineVOs = jobOnlineService.list(job);
		if (CollectionUtils.isNotEmpty(jobOnlineVOs)) {
			ids.addAll(jobOnlineVOs.stream().map(JobOnlineVO::getJobId).collect(Collectors.toList()));
		}
		job = new JobOnline();
		job.setDbSourceId(dbId);
		job.setIsDeleted(false);
		jobOnlineVOs = jobOnlineService.list(job);
		if (CollectionUtils.isNotEmpty(jobOnlineVOs)) {
			ids.addAll(jobOnlineVOs.stream().map(JobOnlineVO::getJobId).collect(Collectors.toList()));
		}
		return ids;
	}
	
	public <T> T get(Long id, LoginUserDto userDto) {
		Assert.notNull(id);
		
		JobVO job = jobService.fromPoToVo(jobService.getById(id));
		if (job == null) {
			return null;
		}
		List<AlarmDto> alarmDtos = getAlarmVo(id);
		JobVo jobDto = getJobDto(job, alarmDtos);
		// 合并jobConfig 和 param
		mergeJobConfigAndParam(jobDto);
		
		// 获取脚本名称
		jobDto.setScriptName(scriptService.getName(job.getScriptId()));
		if (job.getProjectId() != null) {
			jobDto.setProjectName(projectService.getName(job.getProjectId()));
		}
		
		// 是否有权编辑
		if (userDto.getLocalUserId().equals(jobDto.getCreateUser())
				|| BooleanUtils.isTrue(userDto.getIsAdmin())) {
			jobDto.setIsEdit(true);
		} else {
			jobDto.setIsEdit(false);
		}
		
		jobDto.setExecParam(null);
		
		if (JobCategoryEnum.CAL == job.getCategroy()) {
			JobCalDto jobCalDto = new JobCalDto();
			BeanUtil.copyBeanNotNull2Bean(jobDto, jobCalDto);
			JobCal jobcal = jobCalService.getByJobId(id);
			BeanUtil.copyBeanNotNull2Bean(jobcal, jobCalDto);
			jobCalDto.setId(job.getId());
			if (jobcal != null) {
				jobCalDto.setTargetTable(jobcal.getTargetTable());
				jobCalDto.setDbTargetId(jobcal.getDbTargetId());
			}
			return (T) jobCalDto;
		} else if (JobCategoryEnum.COLLECT == job.getCategroy()) {
			JobCollectDto jobCollectDto = new JobCollectDto();
			BeanUtil.copyBeanNotNull2Bean(jobDto, jobCollectDto);
			JobCollect jobCollect = jobCollectService.getByJobId(id);
			BeanUtil.copyBeanNotNull2Bean(jobCollect, jobCollectDto);
			jobCollectDto.setId(job.getId());
			if (jobCollect != null) {
				JobColumnDto jobColumnDto = new JobColumnDto();
				BeanUtil.copyBeanNotNull2Bean(jobCollect, jobColumnDto);
				jobColumnDto.setTargetColumns(string2Lists(jobCollect.getTargetColumns()));
				jobColumnDto.setSourceColumns(string2Lists(jobCollect.getSourceColumns()));
				jobColumnDto.setWhere(jobCollect.getWhereSql());
				jobCollectDto.setColumnDto(jobColumnDto);
			}
			return (T) jobCollectDto;
		} else if (JobCategoryEnum.REFLUE == job.getCategroy()) {
			JobReflueDto jobReflueDto = new JobReflueDto();
			BeanUtil.copyBeanNotNull2Bean(jobDto, jobReflueDto);
			JobReflue jobReflue = jobReflueService.getByJobId(id);
			BeanUtil.copyBeanNotNull2Bean(jobReflue, jobReflueDto);
			jobReflueDto.setId(job.getId());
			if (jobReflue != null) {
				JobColumnDto jobColumnDto = new JobColumnDto();
				BeanUtil.copyBeanNotNull2Bean(jobReflue, jobColumnDto);
				jobColumnDto.setTargetColumns(string2Lists(jobReflue.getTargetColumns()));
				jobColumnDto.setSourceColumns(string2Lists(jobReflue.getSourceColumns()));
				jobColumnDto.setSql(jobReflue.getUserSql());
				jobColumnDto.setWhere(jobReflue.getWhereSql());
				jobReflueDto.setColumnDto(jobColumnDto);
			}
			return (T) jobReflueDto;
		}
		return null;
		
	}

	private List<AlarmDto> getAlarmVo(Long id) {
		List<AlarmDto> alarmDtos = alarmService.getByJobId(id);
		Set<Long> userGroupIds = new HashSet<>();
		for (AlarmDto alarmDto: alarmDtos) {
			userGroupIds.addAll(alarmDto.getUserGroupIds());
		}
		List<GroupInfoVO> groupInfoVos = userInfoBizService.listGroupInfoByIds(Lists.newArrayList(userGroupIds));
		if (CollectionUtils.isEmpty(groupInfoVos)) {
			return alarmDtos;
		}
		Map<Long, GroupInfoVO> groupInfoVOMap = new HashMap<>();
		for (GroupInfoVO groupInfoVO: groupInfoVos) {
			groupInfoVOMap.put(groupInfoVO.getId(), groupInfoVO);
		}
		// 避免fastjson 此处会造成多个元素持有一个问题
		for (AlarmDto alarmDto: alarmDtos) {
			List<GroupInfoVO> newGroupInfoVos = new ArrayList<>();
			for (Long userGroupId: alarmDto.getUserGroupIds()) {
				GroupInfoVO groupInfoVO = new GroupInfoVO();
				BeanUtil.copyBeanNotNull2Bean(groupInfoVOMap.get(userGroupId), groupInfoVO);
				newGroupInfoVos.add(groupInfoVO);
			}
			alarmDto.setUserGroups(newGroupInfoVos);
		}
		return alarmDtos;
	}

	public String getTargetName(Long jobId, JobCategoryEnum jobCategory) {
		Assert.notNull(jobId);
		Assert.notNull(jobCategory);
		
		String targetTable = null;
		if (JobCategoryEnum.CAL == jobCategory) {
			JobCal jobcal = jobCalService.getByJobId(jobId);
			targetTable = jobcal.getTargetTable();
		} else if (JobCategoryEnum.COLLECT == jobCategory) {
			JobCollect jobCollect = jobCollectService.getByJobId(jobId);
			targetTable = jobCollect.getTargetTable();
		} else if (JobCategoryEnum.REFLUE == jobCategory) {
			JobReflue jobReflue = jobReflueService.getByJobId(jobId);
			targetTable = jobReflue.getTargetTable();
		}
		return targetTable;
	}
	
	private void mergeJobConfigAndParam(JobOnlineDto jobDto) {
		if (StringUtils.isBlank(jobDto.getJobConf())) {
			return;
		}
		Map<String, Object> map = JSON.parseObject(jobDto.getJobConf(), Map.class);
		List<ParamContent> paramContents = map.entrySet().stream().map(data -> {
			ParamContent paramContent = new ParamContent();
			paramContent.setName(data.getKey());
			paramContent.setValue(Optional.ofNullable(data.getValue()).orElse("").toString());
			return paramContent;
		}).collect(Collectors.toList());
		
		Optional.ofNullable(jobDto.getSysParams()).orElse(Lists.newArrayList()).addAll(paramContents);
	}
	
	private void mergeJobConfigAndParam(JobDto jobDto) {
		if (StringUtils.isBlank(jobDto.getJobConf())) {
			return;
		}
		Map<String, Object> map = JSON.parseObject(jobDto.getJobConf(), Map.class);
		List<ParamContent> paramContents = map.entrySet().stream().map(data -> {
			ParamContent paramContent = new ParamContent();
			paramContent.setName(data.getKey());
			paramContent.setValue(Optional.ofNullable(data.getValue()).orElse("").toString());
			return paramContent;
		}).collect(Collectors.toList());
		
		Optional.ofNullable(jobDto.getSysParams()).orElse(Lists.newArrayList()).addAll(paramContents);
	}

	private List<ColumnDto> getColumns(Long dbId, String tableName) {
		Assert.notNull(dbId, "dbId");
		Assert.notBlank(tableName, "表名");
		tableName = tableName.trim();
		
		CollectDb db = collectDbService.getById(dbId);
		if (db == null) {
			return Lists.newArrayList();
		}
		
		DbDto dbDto = new DbDto();
		dbDto.setDbType(db.getDsType());
		String dbUrl = getDbUrl(db, tableName);
		dbDto.setDbUrl(dbUrl);
		
		dbDto.setDbUser(db.getDsUser());
		dbDto.setDbPwd(this.decrypt(db.getEncryptPwd(),db.getPwdKey()));
		Operations operations = getOperations(db.getDsType());
		if (operations == null) {
			return Lists.newArrayList();
		}
		operations.init(dbDto, dbUrl);
		String dbName = getDbNameFromTable(tableName, dbDto.getDbName());
		tableName = getTableName(tableName);
		return operations.listColumns(dbUrl, dbName, tableName);
	}


	/**
	 * hive使用库名.表名的库名这个为准，其他的一律用dsUrl本来的为准
	 * 
	 * @param db
	 * @param tableName
	 * @return
	 */
	private String getDbUrl(CollectDb db, String tableName) {
		Assert.notNull(db);
		String dsUrl = db.getDsUrl();
		Assert.notBlank(dsUrl, "数据库连接信息");
		String dbHost = db.getDbHost();
		String dbName = db.getDbName();
		Assert.notBlank(dbHost, "数据库host");
		Assert.notBlank(tableName, "表名");
		Assert.notBlank(dbName, "db名");
		
		if (!StringUtils.contains(tableName, DOT)) {
			return dsUrl;
		}
		String[] tableArr = StringUtils.split(tableName, DOT);
		if (tableArr.length != 2) {
			return dsUrl;	
		}
		
		if (db.getDsType() != DbType.HIVE) {
			return dsUrl;
		}
		
		String reqDbName = StringUtils.trim(tableArr[0]);
		
		String replaceValue = "REPLACE-VALUE";
		dsUrl = dsUrl.replace(dbHost, replaceValue);
		dsUrl = dsUrl.replaceFirst(dbName, reqDbName);
		dsUrl = dsUrl.replace(replaceValue, dbHost);
		return dsUrl;
	}


	private String getDbNameFromTable(String tableName, String dbName) {
		String[] arr = tableName.split("[.]");
		if (arr.length == 2) {
			return arr[0];
		} else {
			return dbName;
		}
	}


	public String getTableName(String tableName) {
		String[] arr = StringUtils.split(tableName, VERTICAL_LINE);
		int len = arr.length;
		String lastEle = arr[len - 1];
		if ("1".equalsIgnoreCase(lastEle)) {
			log.info("getTableNameV1");
			return getTableNameV1(arr);
		} else {
			log.info("getTableNameV0");
			return getTableNameV0(tableName);
		}
	}

	private String getTableNameV1(String[] arr) {
		String tableName = arr[1];
		String pattern = getSubUtil(tableName,"\\{.*?\\}");
		String table_pre = StringUtils.remove(tableName, pattern);

		String formats = arr[2]
				.toLowerCase();
		StringBuilder sdfSb = new StringBuilder();
		if (formats.contains("%y")) {
			sdfSb.append("yyyy");
		}
		if (formats.contains("%m")) {
			sdfSb.append("MM");
		}
		if (formats.contains("%d")) {
			sdfSb.append("dd");
		}

		DateFormat df = new SimpleDateFormat(sdfSb.toString());
		return table_pre + df.format(new Date());
	}

	private String getTableNameV0(String tableName) {
		if (StringUtils.contains(tableName, DOT)) {
			String[] tableArr = StringUtils.split(tableName, DOT);
			if (tableArr.length == 2) {
				tableName = tableArr[1];
			}
		}

		if (StringUtils.contains(tableName, VERTICAL_LINE)) {
			String[] tableArr = StringUtils.split(tableName, VERTICAL_LINE);
			// 解析表名
			try {
				tableName = tableArr[1];
				String pattern = getSubUtil(tableName,"\\{.*?\\}");
				String patternNum = getSubUtil(pattern,"\\d+");
				patternNum = patternNum != null ? patternNum : "01";
				String table_num = String.format("%"+patternNum+"d", 1);
				String table_pre = StringUtils.remove(tableName, pattern);
				return table_pre + table_num;
			} catch (Exception e) {
				log.error("pattern tablename error: "+tableName, e);
				return tableName;
			}
		}
		log.info("pattern tablename: "+ tableName);
		return tableName;
	}

	private String getSubUtil(String soap,String rgex){
		Pattern pattern = Pattern.compile(rgex);// 匹配的模式
		Matcher m = pattern.matcher(soap);
		while (m.find()) {
			return m.group(0);
		}
		return null;
	}
	
	
	public ColumnsVo listColumnsByTable(ColumnQueryDto columnDto) {
		Assert.notNull(columnDto, "columnDto");
		ColumnsVo columnsVo = new ColumnsVo();
		if (columnDto.getDbSourceId() != null && StringUtils.isNotEmpty(columnDto.getSourceTable())) {
			columnsVo.setSourceColumns(getColumns(columnDto.getDbSourceId(), columnDto.getSourceTable()));;
		}
		if (columnDto.getDbTargetId() != null && StringUtils.isNotEmpty(columnDto.getTargetTable())) {
			columnsVo.setTargetColumns(getColumns(columnDto.getDbTargetId(), columnDto.getTargetTable()));;
		}
		return columnsVo;
		
	}

	private Operations getOperations(DbType type) {
		Assert.notNull(type, "数据库类型");
		Operations operations = null;
		if (type == DbType.MYSQL) {
			operations = SpringBean.getBean(MysqlOperations.class);
		} else if (type == DbType.POSTGRESQL) {
			operations = SpringBean.getBean(PostgresqlOperations.class);
		} else if (type == DbType.HIVE) {
			operations = SpringBean.getBean(HiveOperations.class);
		} else if (type == DbType.REDSHIFT) {
			operations = SpringBean.getBean(RedshiftOperations.class);
		} else if (type == DbType.MONGODB) {
			operations = SpringBean.getBean(MongodbOperations.class);
		}
		return operations;
	}
	
	private List<String> string2Lists(String columns) {
		if (StringUtils.isBlank(columns)) {
			return Lists.newArrayList();
		}
		return Lists.newArrayList(StringUtils.split(columns, ","));
	}

	private JobVo getJobDto(Job job, List<AlarmDto> alarmVos) {
		JobVo jobVo = new JobVo();
		jobVo.setAlarms(alarmVos);
		jobVo.setSchedulerTimeDto(job.getSchedulerTimeDto());
		jobVo.setArgsParam(job.getArgsParam());
		if (StringUtils.isNotBlank(job.getParams())) {
			jobVo.setSysParams(JSON.parseArray(job.getParams(), ParamContent.class));
		}
		BeanUtil.copyBeanNotNull2Bean(job, jobVo);
		return jobVo;
	}
	
	private void getJobOnlineDto(JobOnline job, JobOnlineDto jobDto) {
		jobDto.setSchedulerTimeDto(job.getSchedulerTimeDto());
		jobDto.setArgsParam(job.getArgsParam());
		if (StringUtils.isNotBlank(job.getParams())) {
			jobDto.setSysParams(JSON.parseArray(job.getParams(), ParamContent.class));
		}
		BeanUtil.copyBeanNotNull2Bean(job, jobDto);
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
