package com.bigdata.platform.scheduler.web.core.dto;

import com.bigdata.platform.scheduler.dal.enums.AlarmNoticeTypeEnum;
import com.bigdata.platform.scheduler.dal.enums.AlarmTypeEnum;
import com.bigdata.platform.scheduler.web.core.vo.GroupInfoVO;
import com.google.common.collect.Sets;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class AlarmDto implements Serializable {

	private static final long serialVersionUID = 1232331822403218395L;
	
	private Set<AlarmTypeEnum> types = Sets.newHashSet();
	
	private Integer delayDur;
	
	private AlarmNoticeTypeEnum noticeType;

	private Set<Long> userGroupIds = Sets.newHashSet();;

	public void addUserGroupIds(Long userGroupId) {
		this.userGroupIds.add(userGroupId);
	};
	
	public void addTypes(AlarmTypeEnum type) {
		types.add(type);
	}

	private List<GroupInfoVO> userGroups;

}
