package com.clubfactory.platform.scheduler.web.server.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.clubfactory.platform.common.bean.BaseResult;
import com.clubfactory.platform.common.bean.tuple.Tuple2;
import com.clubfactory.platform.common.exception.BizException;
import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.enums.DependTypeEnum;
import com.clubfactory.platform.scheduler.dal.enums.JobCategoryEnum;
import com.clubfactory.platform.scheduler.dal.po.JobOnlineDepends;
import com.clubfactory.platform.scheduler.web.core.dto.AssistantDto;
import com.clubfactory.platform.scheduler.web.core.dto.AssistantJobDependsDto;
import com.clubfactory.platform.scheduler.web.core.dto.AssistantJobDto;
import com.clubfactory.platform.scheduler.web.core.dto.AssistantSimpleJobDependsDto;
import com.clubfactory.platform.scheduler.web.core.dto.AssistantSimpleJobDependsDto.JobDependsDto;
import com.clubfactory.platform.scheduler.web.core.dto.GraphDto;
import com.clubfactory.platform.scheduler.web.core.dto.IGroupDto;
import com.clubfactory.platform.scheduler.web.core.dto.JobCalDto;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.enums.GroupStatusEnum;
import com.clubfactory.platform.scheduler.web.core.service.CollectDbService;
import com.clubfactory.platform.scheduler.web.core.service.GroupService;
import com.clubfactory.platform.scheduler.web.core.service.JobOnlineDependsService;
import com.clubfactory.platform.scheduler.web.core.service.JobOnlineService;
import com.clubfactory.platform.scheduler.web.core.service.JobService;
import com.clubfactory.platform.scheduler.web.core.service.MachineService;
import com.clubfactory.platform.scheduler.web.core.service.ScriptService;
import com.clubfactory.platform.scheduler.web.core.service.UserService;
import com.clubfactory.platform.scheduler.web.server.constant.JobConstant;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.core.vo.ScriptContentVo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AssistantJobBizService {

	@Resource
	JobDetailBizService jobDetailBizService;
	@Resource
	JobBizService jobBizService;
	@Resource
	GroupService groupService;
	@Resource
	UserService userService;
	@Resource
	JobService jobService;
	@Resource
	JobOnlineService jobOnlineService;
	@Resource
	JobOnlineDependsService jobOnlineDependsService;
	@Resource
	CollectDbService collectDbService;
	@Resource
	ScriptBizService scriptBizService;
	@Resource
	ScriptService scriptService;
	@Resource
	MachineService machineService;
	
	@Value("${python3.path.exec}")
	private String PYTHON3;
	
	
	/**
	 * 获取check结果
	 * 
	 * @param scriptId
	 * @param userDto
	 * @return
	 */
	public String checkGroup(Long scriptId, LoginUserDto userDto) {
		ScriptContentVo scriptContentVo = scriptBizService.getScriptContentById(scriptId, null);
		String name = null;
		try {
			IGroupDto iGroupDto = JSON.parseObject(scriptContentVo.getContent(), AssistantJobDto.class);
			name = iGroupDto.getGroupName();
		} catch (Exception e) {
			name = getGroupName(scriptContentVo.getContent());
		}
		return this.checkGroup(name, userDto);
	}
	
	public String checkGroup(String groupName, LoginUserDto userDto) {
		GroupStatusEnum statusEnum = groupService.getStatus(groupName, userDto.getLocalUserId());
		if (GroupStatusEnum.EXIST_UNSELF == statusEnum) {
			throw new BizException(ErrorCode.GROUP_NOT_USER_SELF);
		} else if (GroupStatusEnum.EXIST == statusEnum) {
			return "该组已经存在，对该组的批量操作，会直接覆盖该组下的所有数据，是否继续操作";
		}
		return "这是一个新组，是否继续操作";
	}

	@Transactional
	public Boolean modifyJob(AssistantJobDto assistantJobDto, LoginUserDto userDto) {
		Assert.notNull(assistantJobDto);
		String groupName = assistantJobDto.getGroupName();
		Assert.notBlank(groupName, "组名称");
		Assert.collectionNotEmpty(assistantJobDto.getJobs(), "任务列表");
		Long userId = getUserId(assistantJobDto.getUserId(), userDto);
		Assert.notNull(userId, "用户信息");
		userDto = genUserDto(userDto, userId);
		
		// 检查组信息并返回组id
		Long groupId = modifyGroup(groupName, userId);
		
		// 组装任务列表
		List<JobCalDto> cals = generateCalJobs(assistantJobDto, groupName, userId, groupId);
		
		// 任务名用户归属检查
		checkJobName(userId, cals);
		
		// 检查数据源是否存在
		checkJob(cals);
		
		// 拆分任务，分别做 增，改
		Map<String, Long> jobMap = jobService.getMapByGroupId(groupId);
		modifyJob(userDto, cals, jobMap);
		
		// 下线任务
		if (MapUtils.isNotEmpty(jobMap)) {
			disableJob(userDto, cals, jobMap);
		}
		
		return true;
	}
	

	public Boolean generateJob(AssistantDto dto, LoginUserDto currentUser) {
		ScriptContentVo scriptContentVo = scriptBizService.getScriptContentById(dto.getScriptId(), null);
		if (StringUtils.equals(scriptContentVo.getFileExt(), JobConstant.JSON_EXT)) {
			String content = RegExUtils.removeAll(scriptContentVo.getContent(), JobConstant.JSON_REMARK_REGEX);
			AssistantJobDto assistantJobDto = JSON.parseObject(content, AssistantJobDto.class);
			assistantJobDto.setProjectId(dto.getProjectId());
			return this.modifyJob(assistantJobDto, currentUser);
		} else if (StringUtils.equals(scriptContentVo.getFileExt(), JobConstant.PYTHON_EXT)) {
			String content = scriptContentVo.getContent();
			String userId = currentUser.getLocalUserId().toString();
			String projectId = null;
			if (dto.getProjectId() != null) {
				projectId = dto.getProjectId().toString();
			}
			String funName = "create";
			
			String fileName = getLocalFileName(userId);
			genLocalFile(content, fileName);
			execLocalFile(userId, projectId, funName, fileName);
		}
		return true;
	}

	private void execLocalFile(String userId, String projectId, String funName, String fileName) {
		String exec = String.format("%s %s %s %s %s",PYTHON3, fileName, funName, projectId, userId);
		StringBuilder msgSb = new StringBuilder();
		StringBuilder errorMsgSb = new StringBuilder();
		Process proc = null;
		try {
			// 执行py文件
			proc = Runtime.getRuntime().exec(exec);
			// 用输入输出流来截取结果
			BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				msgSb.append(line);
			}
			while ((line = error.readLine()) != null) {
				errorMsgSb.append(line);
			}
			in.close();
			error.close();
			proc.waitFor();
		} catch (Exception e) {
			throw new BizException(ErrorCode.SCRIPT_LOCAL_FILE_EXEC_ERROR.setParams(fileName), e);
		} finally {
			if (proc != null) {
				proc.destroy();
			}
			deleteLocalFile(fileName);
		}
		// python的log的日志，会进入的错误数据流中，所以做了兼容
		String msg = msgSb.toString();
		String errorMsg = errorMsgSb.toString();
		log.info("用户id: {}, 项目id: {}, 执行方法: {}, 执行文件: {}，日志: {}",userId, projectId, funName, fileName, msg);
		if (StringUtils.isNotBlank(errorMsg)) {
			String resultMsg = getResultMsg(errorMsg);
			this.getMsg(resultMsg, errorMsg);
		}
		if (StringUtils.isNotBlank(msg)) {
			String resultMsg = getResultMsg(msg);
			this.getMsg(resultMsg, msg);
		}
	}

	private void getMsg(String resultMsg, String content) {
		if (StringUtils.isNotBlank(resultMsg)) {
			BaseResult<String> result = JSON.parseObject(resultMsg, BaseResult.class);
			if (result.isSuccess().equals(false)) {
				throw new BizException(ErrorCode.SCRIPT_LOCAL_FILE_EXEC_ERROR.setParams(result.getMessage()));
			}
		} else {
			throw new BizException(ErrorCode.SCRIPT_LOCAL_FILE_EXEC_ERROR.setParams(content));
		}
	}

	private void genLocalFile(String content, String fileName) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(content);
			out.close();
			log.info("{} 文件创建成功", fileName);
		} catch (IOException e) {
		}
	}

	private String getLocalFileName(String userId) {
		String fileName = UUID.randomUUID().toString() + "_" + userId + ".py";
		return fileName;
	}

	private void deleteLocalFile(String fileName) {
		try{
		    File file = new File(fileName);
		    if (file.delete()) {
		        log.info(file.getName() + " 文件已被删除！");
		    } else{
		    	log.error(file.getName() + " 文件删除失败");
		    }
		} catch(Exception e){
			throw new BizException(ErrorCode.SCRIPT_LOCAL_FILE_DELETE_ERROR.setParams(fileName), e);
		}
	}
	

	// 支持两种格式
	public Boolean generateJobDepends(AssistantDto dto, LoginUserDto currentUser) {
		ScriptContentVo scriptContentVo = scriptBizService.getScriptContentById(dto.getScriptId(), null);
		if (StringUtils.equals(scriptContentVo.getFileExt(), JobConstant.JSON_EXT)) {
			String content = RegExUtils.removeAll(scriptContentVo.getContent(), JobConstant.JSON_REMARK_REGEX);
			AssistantSimpleJobDependsDto dependsDto = JSON.parseObject(content, AssistantSimpleJobDependsDto.class);
			if (CollectionUtils.isNotEmpty(dependsDto.getJobDepends())) {
				return this.modifySimpleJobDepends(dependsDto, currentUser);
			} else {
				AssistantJobDependsDto assistantJobDependsDto = JSON.parseObject(content, AssistantJobDependsDto.class);
				return this.modifyJobDepends(assistantJobDependsDto, currentUser);
			}
		} else if (StringUtils.equals(scriptContentVo.getFileExt(), JobConstant.PYTHON_EXT)) {
			String content = scriptContentVo.getContent();
			String userId = currentUser.getLocalUserId().toString();
			String projectId = null;
			if (dto.getProjectId() != null) {
				projectId = dto.getProjectId().toString();
			}
			String funName = "depend";
			
			String fileName = getLocalFileName(userId);
			genLocalFile(content, fileName);
			execLocalFile(userId, projectId, funName, fileName);
		}
		return false;
	}

	private LoginUserDto genUserDto(LoginUserDto userDto, Long userId) {
		if (userDto == null) {
			userDto = new LoginUserDto();
			userDto.setLocalUserId(userId);
		}
		return userDto;
	}
	
	@Transactional
	public Boolean modifySimpleJobDepends(AssistantSimpleJobDependsDto dependsDto, LoginUserDto currentUser) {
		Assert.notNull(dependsDto);
		Long userId = getUserId(dependsDto.getUserId(), currentUser);
		Assert.notNull(userId, "用户信息");
		String groupName = dependsDto.getGroupName();
		List<JobDependsDto> jobDepends = Optional.ofNullable(dependsDto.getJobDepends()).orElse(Lists.newArrayList());
		
		// 获取id
		genId(jobDepends, userId);
		
		Tuple2<List<Long>, Map<Long, Long>> tuple2 = null;
		if (StringUtils.isBlank(groupName)) {
			List<Long> jobIds = jobDepends.stream().map(JobDependsDto::getId).collect(Collectors.toList());
			tuple2 = jobOnlineService.getTuple2(jobIds);
			
		} else {
			// 检查
			Long groupId = modifyGroup(groupName, userId);
			tuple2 = jobOnlineService.getTuple2(groupId);
		}
		
		if (tuple2 != null) {
			// 检查是否合法
			checkJobDepends(jobDepends, tuple2.getField(1), userId);
			// 删除
			jobOnlineDependsService.removeByIds("job_id", tuple2.f0);
			saveBatch(jobDepends,userId);
		}
		return true;
	}


	private void saveBatch(List<JobDependsDto> jobDepends, Long userId) {
		List<JobOnlineDepends> depends = jobDepends.stream().map(dto -> {
			JobOnlineDepends jobOnlineDepends = new JobOnlineDepends();
			jobOnlineDepends.setJobId(dto.getId());
			jobOnlineDepends.setParentId(dto.getParentId());
			jobOnlineDepends.setType(dto.getType());
			jobOnlineDepends.setCreateUser(userId);
			jobOnlineDepends.setUpdateUser(userId);
			return jobOnlineDepends;
		}).collect(Collectors.toList());
		
		jobOnlineDependsService.saveBatch(depends);
	}

	private void checkJobDepends(List<JobDependsDto> jobDepends,  Map<Long, Long> jobUserMap, Long userId) {
		for (JobDependsDto dto : jobDepends) {
			Assert.notNull(dto.getType());
			Assert.notNull(dto.getId());
			Assert.notNull(dto.getParentId());
			if (DependTypeEnum.SELF == dto.getType()) {
				if (!dto.getId().equals(dto.getParentId())) {
					throw new BizException(ErrorCode.JOB_DEPEND_CONFIG_ERROR);
				}
			}
			Long createUser = jobUserMap.get(dto.getId());
			if (createUser == null) {
				throw new BizException(ErrorCode.JOB_NOT_EXISTE);
			} else if (!userId.equals(createUser)) {
				throw new BizException(ErrorCode.JOB_NOT_PERMISSION);
			}
		}
		
	}

	private void genId(List<JobDependsDto> jobDepends, Long userId) {
		if (CollectionUtils.isEmpty(jobDepends)) {
			return;
		}
		Map<String, Long> jobNameMap = jobOnlineService.getMapByCreateUser(userId);
		for (JobDependsDto dto : jobDepends) {
			if (dto.getId() == null) {
				dto.setId(jobNameMap.get(dto.getName()));
			}
			if (dto.getParentId() == null) {
				dto.setParentId(jobNameMap.get(dto.getParentName()));
			}
		}
	}

	@Transactional
	public Boolean modifyJobDepends(AssistantJobDependsDto assistantJobDependsDto, LoginUserDto currentUser) {
		Assert.notNull(assistantJobDependsDto);
		Long userId = getUserId(assistantJobDependsDto.getUserId(), currentUser);
		Assert.notNull(userId, "用户信息");
		String groupName = assistantJobDependsDto.getGroupName();
		
		genId(assistantJobDependsDto, userId);
		checkRepeat(assistantJobDependsDto,  Lists.newArrayList());
		if (StringUtils.isBlank(groupName)) {
			// 通用
			List<Long> jobIds = Lists.newArrayList();
			getJobByParent(assistantJobDependsDto, jobIds);
			getJobByChild(assistantJobDependsDto, jobIds);
			jobOnlineDependsService.removeByIds("job_id", jobIds);
			saveBatch(assistantJobDependsDto, userId);
		} else {
			// 检查
			Long groupId = modifyGroup(groupName, userId);
			Tuple2<List<Long>, Map<Long, Long>> tuple2 = jobOnlineService.getTuple2(groupId);
			checkJob(assistantJobDependsDto, userId, tuple2.f1);
			// 删除
			jobOnlineDependsService.removeByIds("job_id", tuple2.f0);
			saveBatch(assistantJobDependsDto, userId);
		}
		return true;
	}

	private void checkRepeat(GraphDto dto, List<Long> jobIds) {
		jobIds.add(dto.getId());
		if (CollectionUtils.isNotEmpty(dto.getParents())) {
			for (GraphDto graphDto : dto.getParents()) {
				if (jobIds.contains(graphDto.getId())) {
					throw new BizException(ErrorCode.JOB_NAME_REPEAT_ERROR.setParams(graphDto.getId().toString()));
				}
				checkRepeat(graphDto, jobIds);
			}
		}
		if (CollectionUtils.isNotEmpty(dto.getChilds())) {
			for (GraphDto graphDto : dto.getChilds()) {
				if (jobIds.contains(graphDto.getId())) {
					throw new BizException(ErrorCode.JOB_NAME_REPEAT_ERROR.setParams(graphDto.getId().toString()));
				}
				checkRepeat(graphDto, jobIds);
			}
		}
		
		
	}

	private Long getUserId(Long userId, LoginUserDto currentUser) {
		log.info("uesrId: {}", userId);
		log.info("currentUser: {}", JSON.toJSONString(currentUser));
		if (currentUser != null && currentUser.getLocalUserId() != null) {
			userId = currentUser.getLocalUserId();
		}
		return userId;
	}

	private void getJobByParent(GraphDto dto, List<Long> jobIds) {
		if (CollectionUtils.isNotEmpty(dto.getParents())) {
			jobIds.add(dto.getId());
			for (GraphDto graphDto : dto.getParents()) {
				getJobByParent(graphDto, jobIds);
			}
		}
	}
	
	private void getJobByChild(GraphDto dto, List<Long> jobIds) {
		if (CollectionUtils.isNotEmpty(dto.getChilds())) {
			jobIds.add(dto.getId());
			for (GraphDto graphDto : dto.getChilds()) {
				getJobByChild(graphDto, jobIds);
			}
		}
	}

	private void saveBatch(AssistantJobDependsDto assistantJobDependsDto, Long userId) {
		Set<JobOnlineDepends> depends = Sets.newHashSet();
		genParentDepends(assistantJobDependsDto, depends, userId);
		genChildDepends(assistantJobDependsDto, depends, userId);
		jobOnlineDependsService.saveBatch(Lists.newArrayList(depends));
	}

	private void genSelfDepends(GraphDto dto, Long userId,
			Set<JobOnlineDepends> depends) {
		if (BooleanUtils.isTrue(dto.getIsSelfDependent())) {
			JobOnlineDepends jobOnlineDepends = new JobOnlineDepends();
			jobOnlineDepends.setParentId(dto.getId());
			jobOnlineDepends.setJobId(dto.getId());
			jobOnlineDepends.setType(DependTypeEnum.SELF);
			jobOnlineDepends.setCreateUser(userId);
			jobOnlineDepends.setUpdateUser(userId);
			depends.add(jobOnlineDepends);
		}
	}

	private void genParentDepends(GraphDto dto, Set<JobOnlineDepends> depends, Long userId) {
		genSelfDepends(dto, userId, depends);
		if (CollectionUtils.isNotEmpty(dto.getParents())) {
			for (GraphDto graphDto : dto.getParents()) {
				JobOnlineDepends jobOnlineDepends = new JobOnlineDepends();
				jobOnlineDepends.setJobId(dto.getId());
				jobOnlineDepends.setParentId(graphDto.getId());
				jobOnlineDepends.setType(graphDto.getType());
				jobOnlineDepends.setCreateUser(userId);
				jobOnlineDepends.setUpdateUser(userId);
				depends.add(jobOnlineDepends);
				genParentDepends(graphDto, depends, userId);
			}
		}
	}
	
	private void genChildDepends(GraphDto dto, Set<JobOnlineDepends> depends, Long userId) {
		if (CollectionUtils.isNotEmpty(dto.getChilds())) {
			genSelfDepends(dto, userId, depends);
			for (GraphDto graphDto : dto.getChilds()) {
				JobOnlineDepends jobOnlineDepends = new JobOnlineDepends();
				jobOnlineDepends.setParentId(dto.getId());
				jobOnlineDepends.setJobId(graphDto.getId());
				jobOnlineDepends.setType(graphDto.getType());
				jobOnlineDepends.setCreateUser(userId);
				jobOnlineDepends.setUpdateUser(userId);
				depends.add(jobOnlineDepends);
				genChildDepends(graphDto, depends, userId);
			}
		}
	}

	private void checkJob(AssistantJobDependsDto assistantJobDependsDto, Long userId, Map<Long, Long> jobMap) {
		
		checkCurrentJob(userId, jobMap, assistantJobDependsDto.getId());
		checkParentJob(userId, jobMap, assistantJobDependsDto.getParents());
		checkChildJob(userId, jobMap, assistantJobDependsDto.getChilds());
	}

	private void genId(AssistantJobDependsDto assistantJobDependsDto, Long userId) {
		Map<String, Long> jobNameMap = jobOnlineService.getMapByCreateUser(userId);
		checkAndGenId(jobNameMap, assistantJobDependsDto);
		genParentId(jobNameMap, assistantJobDependsDto);
		genChildId(jobNameMap, assistantJobDependsDto);
	}

	private void genParentId(Map<String, Long> jobNameMap, GraphDto dto) {
		if (CollectionUtils.isNotEmpty(dto.getParents())) {
			for (GraphDto subDto : dto.getParents()) {
				checkAndGenId(jobNameMap, subDto);
				genParentId(jobNameMap, subDto);
			}
		
		}
	}

	private void checkAndGenId(Map<String, Long> jobNameMap, GraphDto dto) {
		if (dto.getId() == null) {
			Long id = jobNameMap.get(dto.getName());
			if (id == null) {
				throw new BizException(ErrorCode.JOB_NOT_EXISTE);
			}
			dto.setId(id);
		}
	}
	
	private void genChildId(Map<String, Long> jobNameMap, GraphDto dto) {
		if (CollectionUtils.isNotEmpty(dto.getChilds())) {
			for (GraphDto subDto : dto.getChilds()) {
				checkAndGenId(jobNameMap, subDto);
				genChildId(jobNameMap, subDto);
			}
		}
	}

	private void checkParentJob(Long userId, Map<Long, Long> jobMap, List<GraphDto> dtos) {
		if (CollectionUtils.isNotEmpty(dtos)) {
			for (GraphDto dto : dtos) {
				Long id = dto.getId();
				checkCurrentJob(userId, jobMap, id);
				checkParentJob(userId, jobMap, dto.getParents());
			}
		}
	}

	private void checkCurrentJob(Long userId, Map<Long, Long> jobMap, Long id) {
		Long createUser = jobMap.get(id);
		if (createUser == null) {
			throw new BizException(ErrorCode.JOB_NOT_EXISTE);
		} else if (!userId.equals(createUser)) {
			throw new BizException(ErrorCode.JOB_NOT_PERMISSION);
		}
	}
	
	private void checkChildJob(Long userId, Map<Long, Long> jobMap, List<GraphDto> dtos) {
		if (CollectionUtils.isNotEmpty(dtos)) {
			for (GraphDto dto : dtos) {
				Long id = dto.getId();
				checkCurrentJob(userId, jobMap, id);
				checkChildJob(userId, jobMap, dto.getChilds());
			}
		}
	}


	private void modifyJob(LoginUserDto userDto, List<JobCalDto> cals, Map<String, Long> jobMap) {
		List<JobCalDto> addJobs = Lists.newArrayList();
		List<JobCalDto> editJobs = Lists.newArrayList();
		for (JobCalDto calDto : cals) {
			if (jobMap.get(calDto.getName()) != null) {
				editJobs.add(calDto);
			} else {
				addJobs.add(calDto);
			}
		}
		for (JobCalDto jobCalDto : addJobs) {
			jobDetailBizService.saveCal(jobCalDto, userDto);
		}
		
		for (JobCalDto jobCalDto : editJobs) {
			jobCalDto.setId(jobMap.get(jobCalDto.getName()));
			jobDetailBizService.editCal(jobCalDto, userDto);
		}
	}


	private void disableJob(LoginUserDto userDto, List<JobCalDto> cals, Map<String, Long> jobMap) {
		List<Long> offJobIds = Lists.newArrayList();
		for (Entry<String, Long> job : jobMap.entrySet()) {
			Boolean isExist = false;
			for (JobCalDto calDto : cals) {
				if (StringUtils.equals(job.getKey(), calDto.getName())) {
					isExist = true;
				}
			}
			if (BooleanUtils.isFalse(isExist)) {
				offJobIds.add(job.getValue());
			}
		}
		if (CollectionUtils.isNotEmpty(offJobIds)) {
			jobBizService.disable(offJobIds, userDto);
		}
	}


	private void checkJob(List<JobCalDto> cals) {
		List<String> names = Lists.newArrayList();
		for (JobCalDto dto : cals) {
			if (dto.getDbTargetId() == null) {
				throw new BizException(ErrorCode.DB_NOT_EXIST.setParams(dto.getDbTargetName()));
			}
			if (dto.getScriptId() == null) {
				throw new BizException(ErrorCode.SCRIPT_NOT_EXISTE.setParams(dto.getScriptName()));
			}
			if (dto.getMachineId() == null) {
				throw new BizException(ErrorCode.MACHINE_NOT_EXISTE.setParams(dto.getMachineIp()));
			}
			if (names.contains(dto.getName())) {
				throw new BizException(ErrorCode.JOB_NAME_REPEAT_ERROR.setParams(dto.getName()));
			} else {
				names.add(dto.getName());
			}
		}
		
	}


	private List<JobCalDto> generateCalJobs(AssistantJobDto assistantJobDto, String groupName, Long userId,
			Long groupId) {
		List<JobCalDto> cals = assistantJobDto.getJobs();
		
		List<String> dbTargetNames = cals.stream().map(JobCalDto::getDbTargetName).collect(Collectors.toList());
		Map<String, Long> collectDbMap = collectDbService.getMapByName(dbTargetNames);
		Map<String, Long> scriptNameMap = scriptService.getScriptNameMap();
		Map<String, Long> machineIpMap = machineService.getIdMapByIp();
		cals.forEach(dto -> {
			dto.setCategroy(JobCategoryEnum.CAL);
			dto.setName(groupName+"_"+dto.getName());
			dto.setGroupId(groupId);
			Long machineId = null;
			if (StringUtils.isBlank(dto.getMachineIp())) {
				machineId = 0L;
			} else {
				machineId = machineIpMap.get(dto.getMachineIp());
			}
			dto.setMachineId(machineId);
			dto.setProjectId(assistantJobDto.getProjectId());
			dto.setScriptId(scriptNameMap.get(dto.getScriptName()));
			dto.setCreateUser(userId);
			dto.setUpdateUser(userId);
			dto.setDbTargetId(collectDbMap.get(dto.getDbTargetName()));
		});
		return cals;
	}


	private void checkJobName(Long userId, List<JobCalDto> cals) {
		List<String> jobNames = cals.stream().map(JobCalDto::getName).collect(Collectors.toList());
		List<Long> createUsers = jobService.listCreateUsersByNames(jobNames);
		if (CollectionUtils.isNotEmpty(createUsers)) {
			for (Long createUser : createUsers) {
				if (!userId.equals(createUser)) {
					throw new BizException(ErrorCode.GROUP_JOBNAME_NOT_USER_SELF);
				}
			}
		}
	}


	private Long modifyGroup(String groupName, Long userId) {
		GroupStatusEnum statusEnum = groupService.getStatus(groupName, userId);
		if (GroupStatusEnum.NOT_EXIST == statusEnum) {
			return groupService.addByName(groupName, userId);
		} else if (GroupStatusEnum.EXIST_UNSELF == statusEnum) {
			throw new BizException(ErrorCode.GROUP_NOT_USER_SELF.setParams(userId.toString()));
		}
		return groupService.getByName(groupName).getId();
	}

	private String getResultMsg(String data) {
		Pattern pattern =Pattern.compile("\\{(.*?)\\}");
		Matcher m = pattern.matcher(data);
		while(m.find()) {
			String group = m.group(1);
			if (group.contains("body") && group.contains("message") && group.contains("code")) {
				StringBuilder sb = new StringBuilder();
				sb.append("{");
				sb.append(group);
				sb.append("}");
				return sb.toString();
			}
		}
		return null;
	}
	
	public String getGroupName(String data) {
		Pattern pattern =Pattern.compile("dag_id='(.*?)',");
		Matcher m = pattern.matcher(data);
		while(m.find()) {
			return m.group(1);
		}
		return data;
	}
}