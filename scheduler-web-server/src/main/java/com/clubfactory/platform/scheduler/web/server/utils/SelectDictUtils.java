package com.clubfactory.platform.scheduler.web.server.utils;

import com.clubfactory.platform.scheduler.dal.enums.*;
import com.clubfactory.platform.scheduler.dal.po.JobType;
import com.clubfactory.platform.scheduler.web.server.vo.CommonEnumVo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
public class SelectDictUtils {


    /**
     * 元素去重
     * @param keyExtractor
     * @param <T>
     * @return
     */
    static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 获取集群管理下拉列表所需的枚举
     * @return
     */
    public static Map getStatusAndFeatureEnums(List<JobType> allJobTypes){
        Map<String, List<CommonEnumVo>> response = new HashMap<>(2);
        List<CommonEnumVo> list = Lists.newArrayList();
        // PYTHON JAVA HIVE
        List<JobType> uniqJobTypes = allJobTypes.stream().
                filter(distinctByKey(JobType::getPluginName))
                .collect(Collectors.toList());
        for (JobType jobType: uniqJobTypes){
            list.add(new CommonEnumVo(jobType.getPluginAlias(), jobType.getPluginName()));
        }
        response.put("feature",list);

        list = Lists.newArrayList();
        for (CommonStatus status : CommonStatus.values()){
            list.add(new CommonEnumVo(status));
        }
        response.put("status",list);
        list =Lists.newArrayList();
        for (ClusterTypeEnum typeEnum : ClusterTypeEnum.values()){
            list.add(new CommonEnumVo(typeEnum));
        }
        response.put("type",list);
        return response;
    }

    /**
     * 调度机状态和功能列表
     * @return
     */
    private static Map getSchedulerStatusAndFeatureEnums(List<JobType> allJobTypes){
        Map<String, List<CommonEnumVo>> response = new HashMap<>(2);
        List<CommonEnumVo> list = Lists.newArrayList();
        // CAL_PYTHON  COLLECT_PYTHON
        for (JobType jobType : allJobTypes) {
            CommonEnumVo commonEnumVo = new CommonEnumVo(jobType.getType(), jobType.getFunction());
            list.add(commonEnumVo);
        }
        response.put("feature",list);
        list = Lists.newArrayList();
        for (CommonStatus status : CommonStatus.values()){
            list.add(new CommonEnumVo(status));
        }
        response.put("status",list);
        return response;
    }


    /**
     * 获取调度机相关列表枚举
     * @return
     */
    public static Map getSchedulerNodeEnums(List<JobType> allJobTypes){
        Map<String, Object> response = new HashMap<>(3);
        List<Map<String,Object>> list = Lists.newArrayList();
        for (MachineTypeEnum role : MachineTypeEnum.values()){
            Map<String,Object> roleMap = Maps.newHashMap();
            roleMap.put("key",role.getDesc());
            roleMap.put("value",role.name());
            if (role == MachineTypeEnum.WORKER){
                roleMap.put("isFunctionSelectable",true);
            }else {
                roleMap.put("isFunctionSelectable",false);
            }
            list.add(roleMap);
        }
        response.put("machineRoles",list);
        response.putAll(getSchedulerStatusAndFeatureEnums(allJobTypes));

        return response;
    }

}
