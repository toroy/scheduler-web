package com.zhugeio.platform.scheduler.web.core.service;

import com.alibaba.fastjson.JSON;
import com.zhugeio.platform.scheduler.dal.dao.TokenMapper;
import com.zhugeio.platform.scheduler.dal.po.Token;
import com.zhugeio.platform.scheduler.web.core.enums.CacheEnum;
import com.zhugeio.platform.scheduler.web.core.utils.GuavaCacheUtil;
import com.zhugeio.platform.scheduler.web.core.vo.TokenVO;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenService extends BaseNewService<TokenVO,Token> {

    @Resource
    TokenMapper tokenMapper;

    @PostConstruct
    public void init(){
        setBaseMapper(tokenMapper);
    }
    
    public Boolean isExist(String token) {
    	List<String> tokens = listTokens();
    	if (tokens.contains(token)) {
    		return true;
    	} else {
    		return false;
    	}
    }

    public Long getCreateUser(String token) {
		Token tokenDto = new Token();
		tokenDto.setValue(token);
		TokenVO vo = this.get(tokenDto);
		if (vo == null) {
			return null;
		}
		return vo.getCreateUser();
	}

    public List<String> listTokens() {
    	String key = CacheEnum.TOEKN.getKey();
    	String value = GuavaCacheUtil.get(key);
    	if (StringUtils.isNotBlank(value)) {
    		return JSON.parseArray(value, String.class);
    	}
    	List<TokenVO> tokens = this.list(new Token());
    	if (CollectionUtils.isEmpty(tokens)) {
    		return Lists.newArrayList();
    	}
    	List<String> values = tokens.stream().map(TokenVO::getValue).collect(Collectors.toList());
    	
    	GuavaCacheUtil.put(key, JSON.toJSONString(values));
    	return values;
    }
}
