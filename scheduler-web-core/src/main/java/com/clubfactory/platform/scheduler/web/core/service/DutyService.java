package com.clubfactory.platform.scheduler.web.core.service;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.clubfactory.platform.scheduler.dal.dao.DutyMapper;
import com.clubfactory.platform.scheduler.dal.po.Duty;
import com.clubfactory.platform.scheduler.web.core.vo.DutyVO;

@Service
public class DutyService extends BaseNewService<DutyVO,Duty> {

    @Resource
    DutyMapper dutyMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(dutyMapper);
    }
    
    public List<DutyVO> onDuty() {
    	Duty duty = new Duty();
    	duty.setIsDeleted(false);
    	duty.setWeek(LocalDate.now().getDayOfWeek().getValue());
    	return list(duty);
    }

}
