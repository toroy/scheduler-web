package com.clubfactory.platform.scheduler.web.server.service;

import com.clubfactory.platform.scheduler.dal.enums.AlarmNoticeTypeEnum;
import com.clubfactory.platform.scheduler.dal.po.Alarm;
import com.clubfactory.platform.scheduler.web.core.service.AlarmService;
import com.clubfactory.platform.scheduler.web.core.service.UserService;
import com.clubfactory.platform.scheduler.web.core.vo.AlarmVO;
import com.clubfactory.platform.scheduler.web.server.dto.enums.UserInfoType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Service
public class AlarmBizService {

    @Resource
    AlarmService alarmService;
    @Resource
    UserInfoBizService userInfoBizService;
    @Resource
    UserService userService;

    ExecutorService executorService = new ThreadPoolExecutor(20, 20,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1000));

    public void run() {
        List<AlarmVO> alarmVos = listAlarms();
        if (CollectionUtils.isEmpty(alarmVos)) {
            return;
        }
        Map<Long, Map<Long,List<AlarmVO>>> mapAlarms = alarmVos.stream().collect(Collectors.groupingBy(Alarm::getCreateUser, Collectors.groupingBy(Alarm::getJobId)));
        // 人维度
        for (Map.Entry<Long, Map<Long, List<AlarmVO>>> mapAlarm : mapAlarms.entrySet()) {
            Long createUserId = mapAlarm.getKey();
            String userName = userService.getUserName(createUserId);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    // 任务维度, 不论通知类型和通知方式，地址都是一模一样
                    Map<Set<String>, Long> setsMap = Maps.newHashMap();
                    for (Map.Entry<Long, List<AlarmVO>> map : mapAlarm.getValue().entrySet()) {
                        Long jobId = map.getKey();
                        Map<AlarmNoticeTypeEnum, Set<String>> alarmMap = map.getValue().stream().collect(Collectors.groupingBy(AlarmVO::getNoticeType, Collectors.mapping(AlarmVO::getAddresses, toSet())));
                        Long groupId = null;
                        groupId = getGroupId(groupId, createUserId, userName, setsMap, alarmMap, AlarmNoticeTypeEnum.IM);
                        groupId = getGroupId(groupId, createUserId, userName, setsMap, alarmMap, AlarmNoticeTypeEnum.EMAIL);
                        if (groupId != null) {
                            updateGroupId(jobId, groupId);
                        }
                    }
                }
            });
        }
    }

    @Nullable
    private Long getGroupId(Long groupId, Long createUserId, String userName, Map<Set<String>, Long> setsMap, Map<AlarmNoticeTypeEnum, Set<String>> alarmMap, AlarmNoticeTypeEnum alarmNoticeType) {
        if (groupId != null) {
            return groupId;
        }
        Set<String> addresses = alarmMap.get(alarmNoticeType);

        UserInfoType userInfoType = null;
        if (AlarmNoticeTypeEnum.IM == alarmNoticeType) {
            userInfoType = UserInfoType.IM_ROBOT;
        } else {
            userInfoType = UserInfoType.EMAIL;
        }
        if (CollectionUtils.isNotEmpty(addresses)) {
            if (setsMap.get(addresses) == null) {
                groupId = userInfoBizService.genUserGroupByUserInfoList(createUserId, userName, Lists.newArrayList(addresses), userInfoType);
                setsMap.put(addresses, groupId);
            } else {
                groupId = setsMap.get(addresses);
            }
        }
        return groupId;
    }

    public static void main(String[] args) {
        Set<String> trees = new TreeSet<>();
        trees.add("a");
        trees.add("c");
        trees.add("b");

        for (String str : trees) {
            System.out.println(str);
        }

        Map<Set<String>, Long> sets = Maps.newHashMap();
        sets.put(trees, 1L);

        Set<String> trees2 = new TreeSet<>();
        trees2.add("b");
        trees2.add("c");
        trees2.add("a");
        if (trees.equals(trees2)) {
            System.out.println("true");
        } else {
            System.out.println("false");
        }

        if (sets.get(trees2) != null) {
            System.out.println("true");
        } else {
            System.out.println("false");
        }

    }

    @NotNull
    private List<AlarmVO> listAlarms() {
        Alarm alarm = new Alarm();
        alarm.setIsDeleted(false);
        return alarmService.list(alarm).stream()
                .filter(alarmDto -> StringUtils.isNotBlank(alarmDto.getAddresses()))
                .filter(alarmDto -> alarmDto.getUserGroupId() == null)
                .collect(Collectors.toList());
    }

    private void updateGroupId(Long jobId, Long groupId) {
        Alarm alarmAdd = new Alarm();
        alarmAdd.setIsDeleted(false);
        alarmAdd.setJobId(jobId);
        Map<String, Object> updateMap = Maps.newHashMap();
        updateMap.put("user_group_id", groupId);
        alarmAdd.setUpdateParam(updateMap);
        alarmService.edit(alarmAdd);
    }
}
