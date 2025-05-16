package com.bigdata.platform.scheduler.web.core.utils;

import com.bigdata.platform.scheduler.web.core.service.JobTypeService;
import com.bigdata.platform.scheduler.dal.enums.JobCategoryEnum;
import com.bigdata.platform.scheduler.dal.po.JobType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author xiejiajun
 */
public class JobTypeCache {

    private static final Map<String, JobType> JOB_TYPE_MAP = Maps.newConcurrentMap();

    private static JobTypeService jobTypeService;

    private static void initCache() {
        if (JOB_TYPE_MAP.isEmpty()) {
            synchronized (JobTypeCache.class) {
                if (JOB_TYPE_MAP.isEmpty()) {
                    if (jobTypeService == null) {
                        jobTypeService = SpringBean.getBean(JobTypeService.class);
                    }
                    jobTypeService.allType().forEach(
                            jobType -> JOB_TYPE_MAP.put(jobType.getFunction(), jobType)
                    );
                }
            }
        }
    }

    /**
     * @param function
     * @return
     */
    public static JobType getJobTypeByFunction (String function) {
        initCache();
        return JOB_TYPE_MAP.computeIfAbsent(function, name -> jobTypeService.getByFunction(function.trim()));
    }

    /**
     * @return
     */
    public static Map<String, JobType> allTypeMap() {
        initCache();
        return Collections.unmodifiableMap(JOB_TYPE_MAP);
    }

    /**
     * @return
     */
    public static List<JobType> allType() {
        initCache();
        return Collections.unmodifiableList(Lists.newArrayList(JOB_TYPE_MAP.values()));
    }

    /**
     * 按大类排序返回任务类型
     * @return
     */
    public static Map<JobCategoryEnum, List<JobType>> categoryJobType() {
        return allType().stream()
                .collect(Collectors.groupingBy(JobType::getCategory));
    }

    /**
     * 刷新缓存
     */
    public static void reloadJobTypes() {
        if (JOB_TYPE_MAP.isEmpty()) {
            return;
        }
        synchronized (JobTypeCache.class) {
            if (!JOB_TYPE_MAP.isEmpty()) {
                if (jobTypeService == null) {
                    jobTypeService = SpringBean.getBean(JobTypeService.class);
                }
                JOB_TYPE_MAP.clear();
                jobTypeService.allType().forEach(
                        jobType -> JOB_TYPE_MAP.put(jobType.getPluginName(), jobType)
                );
            }
        }
    }
    
    public static Boolean isStream(JobCategoryEnum categroy, String type) {
    	JobType jobType = getJobTypeByFunction(categroy, type);
    	if (jobType == null) {
    		return false;
    	}
    	return BooleanUtils.isTrue(jobType.getIsStream());
    }

	public static JobType getJobTypeByFunction(JobCategoryEnum categroy, String type) {
		if (categroy == null || StringUtils.isBlank(type)) {
			return null;
		}
		String key = String.format("%s_%s", categroy, type.trim());
		return getJobTypeByFunction(key);
	}
}
