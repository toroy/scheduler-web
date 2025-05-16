package com.bigdata.platform.scheduler.web.core.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.bigdata.platform.scheduler.dal.dao.ParamMapper;
import com.bigdata.platform.scheduler.dal.enums.ProgramTypeEnum;
import com.bigdata.platform.scheduler.dal.po.Param;
import com.bigdata.platform.scheduler.web.core.vo.ParamVO;
import com.google.common.collect.Lists;

@Service
public class ParamService extends BaseNewService<ParamVO,Param> {

    @Resource
    ParamMapper paramMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(paramMapper);
    }

	/**
	 * @param jobType : 形如PYTHON JAVA等
	 * @param programType
	 * @param name
	 * @return
	 */
    public List<String> listNames(String jobType, ProgramTypeEnum programType, String name) {
    	Param param = new Param();
    	param.setJobType(jobType);
    	param.setProgramType(programType);
    	param.setName(name);
    	param.setIsSystem(false);
    	List<ParamVO> params = this.list(param);
    	if (CollectionUtils.isEmpty(params)) {
    		return Lists.newArrayList();
    	}
    	return params.stream().map(ParamVO::getName).distinct().collect(Collectors.toList());
    }
    
    public List<String> listSysNames(String name) {
    	Param param = new Param();
    	param.setName(name);
    	param.setIsSystem(true);
    	List<ParamVO> params = this.list(param);
    	if (CollectionUtils.isEmpty(params)) {
    		return Lists.newArrayList();
    	}
    	return params.stream().map(ParamVO::getName).distinct().collect(Collectors.toList());
    }
    
    public List<String> listSystemKey() {
    	Param param = new Param();
    	param.setIsDeleted(false);
    	param.setIsSystem(true);
    	List<ParamVO> params = this.list(param);
    	if (CollectionUtils.isEmpty(params)) {
    		return Lists.newArrayList();
    	}
    	return params.stream().map(Param::getName).collect(Collectors.toList());
    }
}
