package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.dao.AlarmMapper;
import com.clubfactory.platform.scheduler.dal.enums.AlarmNoticeTypeEnum;
import com.clubfactory.platform.scheduler.dal.enums.AlarmTypeEnum;
import com.clubfactory.platform.scheduler.dal.po.Alarm;
import com.clubfactory.platform.scheduler.dal.po.UserInfo;
import com.clubfactory.platform.scheduler.web.core.dto.AlarmDto;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.vo.AlarmVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service("alarmService")
public class AlarmService extends BaseNewService<AlarmVO, Alarm>  {

    @Resource
    AlarmMapper alarmMapper;

	@Resource
	UserGroupRelService userGroupRelService;


	@PostConstruct
    public void init(){
        setBaseMapper(alarmMapper);
    }

    public Boolean isExist(Long userGroupId) {
		List<AlarmVO> alarmVOS = list(userGroupId);
		return CollectionUtils.isNotEmpty(alarmVOS);
	}

	private Boolean isValidAlarm(AlarmNoticeTypeEnum alarmNoticeType, List<Long> userGroupIds) {
		List<UserInfo> userInfoList = userGroupRelService.listUserInfosByGroupIds(userGroupIds);
		if (CollectionUtils.isEmpty(userInfoList)) {
			throw new BizException(ErrorCode.ALARM_NOT_EXIST_USER);
		}

		switch (alarmNoticeType) {
			case PHONE_NO:
				if (CollectionUtils.isEmpty(
						userInfoList.stream().filter(userInfo -> StringUtils.isNotBlank(userInfo.getPhoneNo())).collect(Collectors.toList()))
				) {
					throw new BizException(ErrorCode.ALARM_NOT_EXIST_PHONE);
				}
				break;
			case EMAIL:
				if (CollectionUtils.isEmpty(
						userInfoList.stream().filter(userInfo -> StringUtils.isNotBlank(userInfo.getEmail())).collect(Collectors.toList()))
				) {
					throw new BizException(ErrorCode.ALARM_NOT_EXIST_EMAIL);
				}
				break;
			case IM:
				if (CollectionUtils.isEmpty(
						userInfoList.stream().filter(userInfo -> StringUtils.isNotBlank(userInfo.getImRobot())).collect(Collectors.toList()))
				) {
					throw new BizException(ErrorCode.ALARM_NOT_EXIST_IM);
				}
				break;
		}
		return true;
	}

	/**
	 * 前置校验告警配置是否合法，当前只校验有没有手机号
	 * @param alarmDtos
	 * @return
	 */
	public Boolean isValidAlarm(List<AlarmDto> alarmDtos) {
		if (CollectionUtils.isEmpty(alarmDtos)) {
			return true;
		}

		for (AlarmDto alarmDto: alarmDtos) {
			if (CollectionUtils.isEmpty(alarmDto.getUserGroupIds())) {
				throw new BizException(ErrorCode.ALARM_CONTENT_INVALID);
			}
			isValidAlarm(alarmDto.getNoticeType(), new ArrayList<>(alarmDto.getUserGroupIds()));
		}
		return true;
	}

	public List<Long> listJobIds(Long userGroupId) {
		List<AlarmVO> alarmVOS = list(userGroupId);
		return alarmVOS.stream().map(AlarmVO::getJobId).collect(Collectors.toList());
	}

	private List<AlarmVO> list(Long userGroupId) {
		Assert.notNull(userGroupId);
		Alarm alarm = new Alarm();
		alarm.setUserGroupId(userGroupId);
		alarm.setIsDeleted(false);
		return this.list(alarm);
	}

	public List<AlarmDto> getByJobId(Long jobId) {
		Assert.notNull(jobId);
		Alarm alarm = new Alarm();
		alarm.setJobId(jobId);
		alarm.setIsDeleted(false);

		List<AlarmDto> alarmDtos = new ArrayList<>();
		List<AlarmVO> alarms = this.list(alarm);
		if (CollectionUtils.isEmpty(alarms)) {
			return alarmDtos;
		}

		Map<AlarmNoticeTypeEnum, List<AlarmVO>> alarmMap = alarms.stream().collect(Collectors.groupingBy(w -> w.getNoticeType()));
		for (Map.Entry<AlarmNoticeTypeEnum, List<AlarmVO>> entry : alarmMap.entrySet()) {
			AlarmNoticeTypeEnum key = entry.getKey();
			List<AlarmVO> val = entry.getValue();
			AlarmDto alarmDto = new AlarmDto();
			alarmDto.setNoticeType(key);
			for (AlarmVO alarmVo : val) {
				if (alarmVo.getDelayDur() != null) {
					alarmDto.setDelayDur(alarmVo.getDelayDur());
				}
				alarmDto.addTypes(alarmVo.getType());
				alarmDto.addUserGroupIds(alarmVo.getUserGroupId());
			}
			alarmDtos.add(alarmDto);
		}
		return alarmDtos;
	}

    public List<Alarm> buildAlarms(AlarmDto alarmDto, Long jobId, Long userId) {
		List<Alarm> alarms = Lists.newArrayList();
		if (alarmDto == null) {
    		return alarms;
		}
    	Assert.collectionNotEmpty(alarmDto.getTypes(), "告警类型");
    	Assert.notNull(alarmDto.getNoticeType(), "告警方式");
		Assert.collectionNonEmpty(alarmDto.getUserGroupIds(), "联系组");
    	Assert.notNull(jobId);
    	Assert.notNull(userId);
    	
    	if (CollectionUtils.isEmpty(alarmDto.getTypes())
    			|| CollectionUtils.isEmpty(alarmDto.getUserGroupIds())) {
    		return alarms;
    	}
    	
    	for (AlarmTypeEnum type : alarmDto.getTypes()) {
    		for (Long userGroupId : alarmDto.getUserGroupIds()) {
	    		Alarm alarm = new Alarm();
	    		alarm.setJobId(jobId);
	    		alarm.setType(type);
	    		alarm.setNoticeType(alarmDto.getNoticeType());
	    		if (AlarmTypeEnum.DELAY == type) {
	    			Assert.notNull(alarmDto.getDelayDur(), "延迟时间");
	    			alarm.setDelayDur(alarmDto.getDelayDur());
	    		}
	    		alarm.setCreateUser(userId);
	    		alarm.setUserGroupId(userGroupId);
	    		alarm.setUpdateUser(userId);
	    		alarms.add(alarm);
    		}
    	}
		return alarms;
    }

	public Boolean save(List<AlarmDto> alarmDtos, Long jobId, Long userId) {
		if (CollectionUtils.isEmpty(alarmDtos)) {
			return false;
		}
		List<Alarm> alarms = Lists.newArrayList();
		for (AlarmDto alarmDto: alarmDtos) {
			alarms.addAll(buildAlarms(alarmDto, jobId, userId));
		}
		if (CollectionUtils.isEmpty(alarms)) {
			return false;
		}
		this.saveBatch(alarms);
		return true;

	}

    public Boolean del(Long jobId) {
    	Alarm alarm = new Alarm();
    	alarm.setJobId(jobId);
    	this.logicRemove(alarm);
    	return true;
    }
    
    public Boolean del(List<Long> jobIds, Long userId, Boolean isAdmin) {
    	Alarm alarm = new Alarm();
    	alarm.setIds(jobIds);
    	alarm.setQueryListFieldName("job_id");
    	if (BooleanUtils.isFalse(isAdmin)) {
    		alarm.setCreateUser(userId);
    	}
    	this.logicRemove(alarm);
    	return true;
    }
    
    private List<AlarmVO> list(Long jobId, Long userId, Boolean isAdmin) {
    	Alarm alarm = new Alarm();
    	alarm.setJobId(jobId);
    	alarm.setIsDeleted(false);
    	if (BooleanUtils.isFalse(isAdmin)) {
    		alarm.setCreateUser(userId);
    	}
    	return this.list(alarm);
    }

    
    public List<Alarm> listByJobId(Long jobId) {
		if (jobId == null) {
			return Lists.newArrayList();
		}

		Alarm alarm = new Alarm();
    	alarm.setJobId(jobId);
    	alarm.setIsDeleted(false);
    	return alarmMapper.list(alarm);
    }

	public List<Alarm> listByJobIds(List<Long> jobIds) {
		if (CollectionUtils.isEmpty(jobIds)) {
			return Lists.newArrayList();
		}

		Alarm alarm = new Alarm();
		alarm.setQueryListFieldName("job_id");
		alarm.setIds(jobIds);
		alarm.setIsDeleted(false);
		return alarmMapper.list(alarm);
	}

    public void copy(Long jobId, Long targetJobId, Long userId) {
    	Assert.notNull(jobId);
    	Assert.notNull(targetJobId);
    	Assert.notNull(userId);
    	
    	List<Alarm> alarmVOs = this.listByJobId(jobId);
    	if (CollectionUtils.isEmpty(alarmVOs)) {
    		return;
    	}
    	for (Alarm vo : alarmVOs) {
    		vo.setCreateUser(userId);
    		vo.setUpdateUser(userId);
    		vo.setJobId(targetJobId);
    	}
    	this.saveBatch(alarmVOs);
    }
    
    public AlarmDto getAlarm(Long jobId, Long userId, Boolean isAdmin) {
    	Assert.notNull(jobId);
    	Assert.notNull(userId);
    	Assert.notNull(isAdmin);
    	
    	List<AlarmVO> alarmVOs = list(jobId, userId, isAdmin);
    	if (CollectionUtils.isEmpty(alarmVOs)) {
    		return null;
    	}
    	AlarmDto dto = new AlarmDto();
    	for (AlarmVO alarmVO : alarmVOs) {
    		dto.addTypes(alarmVO.getType());
    	}
    	AlarmVO alarmVO = alarmVOs.get(0);
    	dto.setNoticeType(alarmVO.getNoticeType());
    	dto.setDelayDur(alarmVO.getDelayDur());
    	return dto;
    }
    
    public Boolean edit(List<AlarmDto> alarmDtos, Long jobId, Long userId) {
    	this.del(jobId);
    	this.save(alarmDtos, jobId, userId);
    	return true;
    }
    
	public void editOwnerByJobIds(Long userId, Long targetUserId, List<Long> ids) {
		Assert.notNull(userId);
		Assert.notNull(targetUserId);
		Assert.collectionNotEmpty(ids, "任务id列表");
		
		Alarm alarm = new Alarm();
		alarm.setIsDeleted(false);
		alarm.setCreateUser(userId);
		alarm.setIds(ids);
		alarm.setQueryListFieldName("job_id");
		Map<String, Object> updateParam = Maps.newHashMap();
		alarm.setUpdateParam(updateParam);
		updateParam.put("create_user", targetUserId);
		this.edit(alarm);
	}


	public Boolean checkAlarmValidByJobIds(List<Long> jobIds, Long userGroupId) {
		if (CollectionUtils.isEmpty(jobIds)) {
			return true;
		}

		List<Alarm> alarms = listByJobIds(jobIds);
		for (Alarm alarm: alarms) {
			isValidAlarm(alarm.getNoticeType(), Arrays.asList(userGroupId));
		}
		return true;
	}

	public void editUserGroupIdByJobIds(List<Long> jobIds, Long userGroupId) {
		if (CollectionUtils.isEmpty(jobIds)) {
			return;
		}

		Alarm alarm = new Alarm();
		alarm.setQueryListFieldName("job_id");
		alarm.setIds(jobIds);
		alarm.setIsDeleted(false);

		Map<String, Object> updateParam = Maps.newHashMap();
		updateParam.put("user_group_id", userGroupId);
		alarm.setUpdateParam(updateParam);
		this.edit(alarm);
	}

}
