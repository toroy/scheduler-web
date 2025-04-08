package com.clubfactory.platform.scheduler.web.core.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.clubfactory.platform.common.util.Assert;
import com.clubfactory.platform.scheduler.dal.dao.GroupMapper;
import com.clubfactory.platform.scheduler.dal.po.Group;
import com.clubfactory.platform.scheduler.web.core.enums.GroupStatusEnum;
import com.clubfactory.platform.scheduler.web.core.vo.GroupVO;

@Service
public class GroupService extends BaseNewService<GroupVO,Group> {

    @Resource
    GroupMapper groupMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(groupMapper);
    }
    
    public Group getByName(String name) {
    	Assert.notBlank(name);
    	
    	Group group = new Group();
    	group.setName(name);
    	group.setIsDeleted(false);
    	Group groupRep = this.get(group);
    	if (groupRep == null) {
    		return null;
    	}
    	return groupRep;
    }
    
    public GroupStatusEnum getStatus(String name, Long userId) {
    	Assert.notBlank(name, "组名称");
    	Assert.notNull(userId);
    	
    	Group group = this.getByName(name);
    	if (group == null) {
    		return GroupStatusEnum.NOT_EXIST;
    	} 
    	
    	if (group.getCreateUser().equals(userId)) {
    		return GroupStatusEnum.EXIST;
    	} else {
    		return GroupStatusEnum.EXIST_UNSELF;
    	}
    	
    }
    
    public Boolean isExist(String name) {
    	Group group = this.getByName(name);
    	if (group == null) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    public Long addByName(String name, Long userId) {
    	Assert.notBlank(name);
        Group group = new Group();
    	group.setName(name);
    	group.setCreateUser(userId);
    	group.setUpdateUser(userId);
    	this.save(group);
    	return group.getId();
    }

}
