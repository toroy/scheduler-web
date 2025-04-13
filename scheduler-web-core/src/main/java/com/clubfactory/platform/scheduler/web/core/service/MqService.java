package com.clubfactory.platform.scheduler.web.core.service;

import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.common.util.BeanUtil;
import com.clubfactory.platform.scheduler.dal.dao.MqMapper;
import com.clubfactory.platform.scheduler.dal.po.Mq;
import com.clubfactory.platform.scheduler.web.core.dto.MqDto;
import com.clubfactory.platform.scheduler.web.core.vo.MqVO;
import com.google.common.collect.Maps;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

@Service
public class MqService extends BaseNewService<MqVO,Mq> {

    @Resource
    MqMapper mqMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(mqMapper);
    }
    
    public Long saveMqDto(MqDto mqDto, Long userId) {
    	Assert.notNull(mqDto);
    	
    	MqVO mq = new MqVO();
    	BeanUtil.copyBeanNotNull2Bean(mqDto, mq);
    	mq.setCreateUser(userId);
    	mq.setUpdateUser(userId);
    	this.save(mq);
		return mq.getId();
    }
    
    public Boolean editJobIdById(Long jobId, Long id) {
    	Assert.notNull(id);
    	Assert.notNull(jobId);
    	
    	MqVO mq = new MqVO();
    	mq.setId(id);
    	
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("job_id", jobId);
    	mq.setUpdateParam(updateParam);
    	this.edit(mq);
    	return true;
    }
    
    public Mq getById(Long id, Long userId, Boolean isAdmin) {
    	Assert.notNull(id);
    	MqVO mq = new MqVO();
    	mq.setId(id);
    	if (BooleanUtils.isNotTrue(isAdmin)) {
    		mq.setCreateUser(userId);
    	}
    	return this.get(mq);
    }
    
    public Mq getById(Long id) {
    	Assert.notNull(id);
    	MqVO mq = new MqVO();
    	mq.setId(id);
    	return this.get(mq);
    }
    
    public Mq getByJobId(Long jobId) {
    	Assert.notNull(jobId);
    	MqVO mq = new MqVO();
    	mq.setJobId(jobId);
    	return this.get(mq);
    }
    
    public Boolean editMqDto(MqDto mqDto, Long userId) {
    	Assert.notNull(mqDto);
    	
    	MqVO mq = new MqVO();
    	mq.setId(mqDto.getId());
    	
    	Map<String, Object> updateParam = Maps.newHashMap();
    	updateParam.put("db_id", mqDto.getDbId());
    	updateParam.put("update_user", userId);
    	updateParam.put("topic_name", mqDto.getTopicName());
		updateParam.put("table_name", mqDto.getTableName());
    	updateParam.put("topic_desc", mqDto.getTopicDesc());
    	updateParam.put("offset_type", mqDto.getOffsetType());
    	updateParam.put("data_type", mqDto.getDataType());
    	updateParam.put("field_delimiter", mqDto.getFieldDelimiter());
    	mq.setUpdateParam(updateParam);
    	this.edit(mq);
		return true;
    }

}
