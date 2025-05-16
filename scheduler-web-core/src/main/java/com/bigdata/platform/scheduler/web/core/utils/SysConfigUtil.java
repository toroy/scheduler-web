package com.bigdata.platform.scheduler.web.core.utils;



import com.bigdata.platform.scheduler.web.core.enums.CacheEnum;
import org.apache.commons.lang3.StringUtils;

import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.dal.dao.SysConfigMapper;
import com.bigdata.platform.scheduler.dal.enums.ConfigType;
import com.bigdata.platform.scheduler.dal.po.SysConfig;

public class SysConfigUtil {

	static SysConfigMapper sysConfigMapper;
	
	private static final String DEFAULT = "1";
	
	static {
		sysConfigMapper = SpringBean.getBean(SysConfigMapper.class);
	}
	
    public static String getByKey(String key) {
    	Assert.notNull(key);
    	
    	
    	String cacheKey = CacheEnum.SYS_CONFIG.getKey(key);
    	String value = GuavaCacheUtil.get(cacheKey);
    	if (value != null) {
    		return value;
    	}
    	if (StringUtils.equals(value, DEFAULT)) {
    		return null;
    	}
    	
    	SysConfig sysConfig = SysConfig.builder().paramKey(key).configType(ConfigType.WEB).build();
    	SysConfig res = sysConfigMapper.get(sysConfig);
    	
    	if (res == null) {
    		GuavaCacheUtil.put(cacheKey, DEFAULT);
    		return null;
    	}
    	
    	GuavaCacheUtil.put(cacheKey, res.getParamValue());
    	return res.getParamValue();
    }
    
    public static Integer getNumberByKey(String key) {
    	
    	String value = SysConfigUtil.getByKey(key);
    	if (value == null) {
    		return null;
    	}
    	
    	try {
    		return Integer.valueOf(value);
		} catch (Exception e) {
			return null;
		}
    }
    
    public static Long getLongByKey(String key) {
    	
    	String value = SysConfigUtil.getByKey(key);
    	if (value == null) {
    		return null;
    	}
    	
    	try {
    		return Long.valueOf(value);
		} catch (Exception e) {
			return null;
		}
    }
}
