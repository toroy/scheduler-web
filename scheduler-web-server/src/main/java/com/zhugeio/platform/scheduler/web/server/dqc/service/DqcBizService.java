package com.zhugeio.platform.scheduler.web.server.dqc.service;

import com.zhugeio.platform.scheduler.web.server.dqc.dto.ContentDto;
import com.zhugeio.platform.scheduler.web.server.dqc.dto.DqcRunDto;
import com.zhugeio.platform.scheduler.web.server.dqc.dto.DqcTaskDto;
import com.zhugeio.platform.scheduler.web.server.dqc.enums.DqcTaskStatusEnum;
import com.zhugeio.platform.scheduler.web.server.dqc.vo.DqcDetailVo;
import com.zhugeio.platform.scheduler.web.server.dqc.vo.DqcRunVo;
import com.zhugeio.platform.scheduler.web.server.dqc.vo.DqcTableRuleVo;
import com.zhugeio.platform.scheduler.web.server.dqc.vo.DqcTaskVo;
import com.zhugeio.platform.scheduler.web.server.login.LoginUserDto;
import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.common.bean.tuple.Tuple3;
import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.common.util.BeanUtil;
import com.zhugeio.platform.meta.client.dto.TableOwnerDto;
//import com.zhugeio.platform.meta.client.service.MetaClientService;
import com.zhugeio.platform.meta.client.utils.DbUtil;
import com.zhugeio.platform.scheduler.dal.enums.JobTypeEnum;
import com.zhugeio.platform.scheduler.dal.enums.TaskStatusEnum;
import com.zhugeio.platform.scheduler.dal.po.DqcRule;
import com.zhugeio.platform.scheduler.dal.po.DqcTask;
import com.zhugeio.platform.scheduler.dal.po.JobOnline;
import com.zhugeio.platform.scheduler.web.core.dqc.service.DqcPartitionService;
import com.zhugeio.platform.scheduler.web.core.dqc.service.DqcRuleService;
import com.zhugeio.platform.scheduler.web.core.dqc.service.DqcTableService;
import com.zhugeio.platform.scheduler.web.core.dqc.service.DqcTaskService;
import com.zhugeio.platform.scheduler.web.core.dqc.vo.DqcRuleVO;
import com.zhugeio.platform.scheduler.web.core.enums.ErrorCode;
import com.zhugeio.platform.scheduler.web.core.service.*;
import com.zhugeio.platform.scheduler.web.core.vo.TaskVO;
import com.zhugeio.platform.scheduler.web.server.service.JobBizService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DqcBizService {

//    @Resource
//    MetaClientService metaClientService;
    @Resource
UserService userService;
    @Resource
    DqcRuleService dqcRuleService;
    @Resource
    JobOnlineService jobOnlineService;
    @Resource
    TaskDependsService taskDependsService;
    @Resource
    TaskService taskService;
    @Resource
    DqcTableService dqcTableService;
    @Resource
    DqcPartitionService dqcPartitionService;
    @Resource
    DqcPartitionBizService dqcPartitionBizService;
    @Resource
    JobBizService jobBizService;
    @Resource
    DqcTaskService dqcTaskService;
    @Resource
    DqcRuleBizService dqcRuleBizService;
    @Resource
    AlarmService alarmService;

    ExecutorService executors = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(100));

    private final static String PARENT_ID = "parent_id";
    private final static String TASK_IDS = "task_ids";

    public PageUtils<DqcTableRuleVo> listByPage(String dbName, String tableName, Integer pageNo, Integer pageSize) {

//        BaseResult<PageUtils<TableDto>> baseResult = metaClientService.listHiveTableByPage(dbName, tableName,  pageSize, pageNo);
//        if (baseResult.isSuccess().equals(false) || CollectionUtils.isEmpty(baseResult.getBody().getRows())) {
//            return new PageUtils<DqcTableRuleVo>(pageSize, pageNo);
//        }
//        PageUtils<TableDto> tableDtoPageUtils = baseResult.getBody();
//        List<TableDto> tableDtos = tableDtoPageUtils.getRows();
//
//        // 获取用户名
//        List<String> uids = tableDtos.stream().filter(dto -> dto.getUid() != null).map(TableDto::getUid).collect(Collectors.toList());
//        Map<String, String> userNameMap = userService.getUserNameMapByUid(uids);
//
//        // 获取规则数
//        List<String> tableNames = tableDtos.stream().map(TableDto::getName).collect(Collectors.toList());
//        List<DqcRuleVO> ruleVOS = dqcRuleService.listByTableNames(tableNames);
//        Map<String, List<String>>  dbTableMap = ruleVOS.stream().map(rule -> getDbTable(rule.getDbName(), rule.getTableName())).collect(Collectors.groupingBy(s -> s));
//
//        // 组装参数
//        List<DqcTableRuleVo> vos = tableDtos.stream().map(dto -> {
//            DqcTableRuleVo dqcTableRuleVo = new DqcTableRuleVo();
//            if (dbTableMap.get(getDbTable(dto.getDbName(),dto.getName())) != null) {
//                dqcTableRuleVo.setRuleNum(dbTableMap.get(getDbTable(dto.getDbName(), dto.getName())).size());
//            }
//            dqcTableRuleVo.setDbName(dto.getDbName());
//            dqcTableRuleVo.setTableName(dto.getName());
//            if (dto.getUid() != null) {
//                dqcTableRuleVo.setUserName(userNameMap.get(dto.getUid().toString()));
//            }
//            dqcTableRuleVo.setRuleId(dto.getDbName()+"."+dto.getName());
//            return dqcTableRuleVo;
//        }).collect(Collectors.toList());
//        return new PageUtils<>(vos, tableDtoPageUtils.getTotalCount(), tableDtoPageUtils.getPageSize(), tableDtoPageUtils.getPageNo());
        return new PageUtils<DqcTableRuleVo>();
    }

    private String getDbTable(String dbName, String tableName) {
        return dbName + "_" + tableName;
    }

    @Transactional
    public Boolean relJob(String dbName, String tableName, Long relJobId, LoginUserDto userDto) {
        Assert.notBlank(dbName);
        Assert.notBlank(tableName);
        Assert.notNull(relJobId);
        this.isOwner(dbName, tableName, userDto.getUserid());

        // 更新到dqc线下任务表里
        JobOnline jobOnline = jobOnlineService.getById(relJobId);
        if (jobOnline == null) {
            throw new BizException(ErrorCode.JOB_NOT_ONLINE_ERROR);
        }
        List<Long> jobIds = dqcRuleService.listJobIds(dbName, tableName);

        // 更新关联id
        dqcRuleService.updateRelJobId(dbName, tableName, relJobId);
        if (CollectionUtils.isEmpty(jobIds)) {
            return true;
        }
        // 更改调度时间，周期， 并上线
        dqcRuleBizService.updateJobScheduler(relJobId, jobIds, jobOnline.getSchedulerTimeDto(), jobOnline.getCycleType(), userDto);

        // 记录关联任务id
        Long dqcRelJobId = dqcTableService.getRelJobId(dbName, tableName);
        if (dqcRelJobId == null) {
            dqcTableService.save(dbName, tableName, relJobId, userDto.getLocalUserId());
        } else if (!dqcRelJobId.equals(relJobId)) {
            dqcTableService.update(dbName, tableName, relJobId, userDto.getLocalUserId());
        }

        // 新增报警
        for (Long jobId : jobIds) {
            if (dqcRelJobId == null) {
                dqcRuleBizService.saveAlarms(userDto, jobId, relJobId);
            } else if (!dqcRelJobId.equals(relJobId)) {
                alarmService.del(jobId);
                dqcRuleBizService.saveAlarms(userDto, jobId, relJobId);
            }
        }
        return true;
    }

    /**
     * 根据实例id 获取规则的实例id信息
     *
     * @param parentTaskId 关联任务的实例id
     * @return List<Tuple3<Long, TaskStatusEnum, String>> 实例Id, 实例状态，规则名称
     */
    public List<Tuple3<Long, TaskStatusEnum, String>> listDqcTaskByTaskId(Long parentTaskId) {
        List<Long> taskIds = taskDependsService.listIdsByParent(parentTaskId);
        if (CollectionUtils.isEmpty(taskIds)) {
            return Lists.newArrayList();
        }
        List<TaskVO> taskVOS = taskService.listDqcByIds(taskIds);
        if (CollectionUtils.isEmpty(taskVOS)) {
            return Lists.newArrayList();
        }
        return taskVOS.stream().map(taskVO -> Tuple3.of(taskVO.getId(), taskVO.getStatus(), taskVO.getName())).collect(Collectors.toList());
    }

    public PageUtils<DqcTaskVo> listTaskRulesByPage(DqcTaskDto dqcTaskDto) {

        DqcTask dqcTask = new DqcTask();
        dqcTask.setDbName(dqcTaskDto.getDbName());
        dqcTask.setTableName(dqcTaskDto.getTableName());
        dqcTask.setTargetTaskId(dqcTaskDto.getTargetTaskId());
        dqcTask.setStartDate(dqcTaskDto.getStartTime());
        dqcTask.setEndDate(dqcTaskDto.getEndTime());
        if (dqcTaskDto.getStatus() != null) {
            dqcTask.setStatus(dqcTaskDto.getStatus().name());
        }
        dqcTask.setPageNo(dqcTaskDto.getPageNo());
        dqcTask.setPageSize(dqcTaskDto.getPageSize());
        dqcTask.setOrderBy("exec_time desc");
        PageUtils<DqcTask> pageUtils = dqcTaskService.pageList(dqcTask);

        if (CollectionUtils.isEmpty(pageUtils.getRows())) {
            return new PageUtils<DqcTaskVo>();
        }

        List<DqcTask> dqcTasks = pageUtils.getRows();
        List<DqcTaskVo> dqcTaskVos = dqcTasks.stream().map(task -> {
            DqcTaskVo dqcTaskVo = new DqcTaskVo();
            BeanUtil.copyBeanNotNull2Bean(task, dqcTaskVo);
            dqcTaskVo.setPartition(task.getPartitionValue());
            dqcTaskVo.setStatus(DqcTaskStatusEnum.valueOf(task.getStatus()).getDesc());
            dqcTaskVo.setStatusType(DqcTaskStatusEnum.valueOf(task.getStatus()));
            return dqcTaskVo;
        }).collect(Collectors.toList());

        return new PageUtils<DqcTaskVo>(dqcTaskVos, pageUtils.getTotalCount(), pageUtils.getPageSize(), pageUtils.getPageNo());
    }

    public void genDqcTask() {
        // 获取DQC的信息
        List<Map<String, Object>> targetTaskIds = dqcRuleService.listMap();
        if (CollectionUtils.isEmpty(targetTaskIds)) {
            return;
        }

        // 所有实例信息
        List<TaskVO> taskVOS = listTaskVos(targetTaskIds);

        // dqc信息
        List<Long> jobIds = taskVOS.stream().filter(taskVO -> JobTypeEnum.DQC == taskVO.getJobType())
                .map(TaskVO::getJobId)
                .distinct()
                .collect(Collectors.toList());
        List<DqcRuleVO> ruleVOS = dqcRuleService.list(jobIds);

        // 组装数据
        List<DqcTask> dqcTaskVos = genDqcTaskVos(targetTaskIds, taskVOS, ruleVOS);

        // 操作数据
        dqcTaskService.remove(new DqcTask());
        dqcTaskService.saveBatch(dqcTaskVos);
        return;
    }

    @NotNull
    private List<DqcTask> genDqcTaskVos(List<Map<String, Object>> targetTaskIds, List<TaskVO> taskVOS, List<DqcRuleVO> ruleVOS) {
        Map<Long, List<TaskVO>> taskMap = taskVOS.stream().collect(Collectors.groupingBy(TaskVO::getId));
        Map<Long, List<DqcRuleVO>> ruleMap = ruleVOS.stream().collect(Collectors.groupingBy(DqcRuleVO::getJobId));
        Map<Long, String>  partitionMap = dqcPartitionService.getExpressionMap(ruleVOS.stream().map(DqcRuleVO::getDqcPartitionId).distinct().collect(Collectors.toList()));
        List<DqcTask> dqcTaskVos = Lists.newArrayList();
        long i = 1;
        for (Map<String, Object> targetMap : targetTaskIds) {
            Long targetId = Long.valueOf(targetMap.get(PARENT_ID).toString());
            if (taskMap.get(targetId) == null) {
                continue;
            }
            TaskVO taskVO = taskMap.get(targetId).get(0);
            String taskIdsString = targetMap.get(TASK_IDS).toString();
            String[] taskIdStringArr = taskIdsString.split(",");

            List<TaskVO> errorTasks = listErrorTasks(taskMap, taskIdStringArr);

            Date execTime = getExecTime(taskMap, taskIdStringArr);

            Tuple3<DqcRuleVO, DqcTaskStatusEnum, Set<Long>> tuple = getRuleTuple(taskMap, ruleMap, taskIdStringArr);

            String partitionString = getPartition(partitionMap, taskVO, tuple.f2);
            DqcTask dqcTaskVo = new DqcTask();
            dqcTaskVo.setTargetTaskId(targetId);
            dqcTaskVo.setId(i++);
            dqcTaskVo.setOwnerName(userService.getUserName(taskVO.getCreateUser()));
            dqcTaskVo.setTaskTime(taskVO.getTaskTime());
            dqcTaskVo.setExecTime(execTime);
            dqcTaskVo.setTableName(tuple.f0.getTableName());
            dqcTaskVo.setDbName(tuple.f0.getDbName());
            dqcTaskVo.setPartitionValue(partitionString);
            dqcTaskVo.setRuleNum(taskIdStringArr.length);
            dqcTaskVo.setExceptionNum(errorTasks.size());
            dqcTaskVo.setStatus(tuple.f1.name());
            dqcTaskVo.setCreateUser(taskVO.getCreateUser());
            dqcTaskVo.setUpdateUser(taskVO.getCreateUser());
            dqcTaskVos.add(dqcTaskVo);
        }
        return dqcTaskVos;
    }

    @NotNull
    private Date getExecTime(Map<Long, List<TaskVO>> taskMap, String[] taskIdStringArr) {
        List<TaskVO> tasks = listTasks(taskMap, taskIdStringArr);
        return tasks.stream().map(task-> {
            if (task.getExecTime() != null) {
                return task.getExecTime();
            }
            return task.getStartTime();
        }).min(new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                return o1.compareTo(o2);
            }
        }).get();
    }

    @NotNull
    private Tuple3<DqcRuleVO, DqcTaskStatusEnum, Set<Long>> getRuleTuple(Map<Long, List<TaskVO>> taskMap, Map<Long, List<DqcRuleVO>> ruleMap, String[] taskIdStringArr) {
        Tuple3<DqcRuleVO, DqcTaskStatusEnum, Set<Long>> tuple = new Tuple3<>();
        DqcRuleVO dqcRuleVO = null;
        DqcTaskStatusEnum dqcTaskStatus = DqcTaskStatusEnum.INIT;
        Set<Long> dqcPartitions = Sets.newHashSet();
        for (String taskIdString : taskIdStringArr) {
            Long taskId = Long.valueOf(taskIdString);
            TaskVO task = taskMap.get(taskId).get(0);
            dqcRuleVO = ruleMap.get(task.getJobId()).get(0);
            dqcPartitions.add(dqcRuleVO.getDqcPartitionId());

            // 获取实例状态
            if (TaskStatusEnum.DATA_FAILED == task.getStatus()
                    || TaskStatusEnum.FAILED == task.getStatus() ) {
                if (dqcRuleVO.getIsBlock().equals(true)) {
                    dqcTaskStatus = DqcTaskStatusEnum.BLOCK;
                } else if (dqcTaskStatus != DqcTaskStatusEnum.BLOCK) {
                    dqcTaskStatus = DqcTaskStatusEnum.ALARM;
                }
            } else if (TaskStatusEnum.SUCCESS == task.getStatus()
                && dqcTaskStatus == DqcTaskStatusEnum.INIT) {
                dqcTaskStatus = DqcTaskStatusEnum.SUCCESS;
            }
        }
        tuple.setFields(dqcRuleVO, dqcTaskStatus, dqcPartitions);
        return tuple;
    }

    private String getPartition(Map<Long, String> partitonMap, TaskVO taskVO, Set<Long> dqcPartitions) {
        List<String> partitions = dqcPartitions.stream().map(partitionId -> {
            return dqcPartitionBizService.cal(partitonMap.get(partitionId), taskVO.getTaskTime());
        }).collect(Collectors.toList());
        return StringUtils.join(partitions, " ");
    }

    @NotNull
    private List<TaskVO> listErrorTasks(Map<Long, List<TaskVO>> taskMap, String[] taskIdStringArr) {
        List<TaskVO> errorTasks = Lists.newArrayList();
        for (String taskIdString : taskIdStringArr) {
            Long taskId = Long.valueOf(taskIdString);
            TaskVO task = taskMap.get(taskId).get(0);
            if (TaskStatusEnum.DATA_FAILED == task.getStatus()
                    || TaskStatusEnum.FAILED == task.getStatus() ) {
                errorTasks.add(task);
            }
        }
        return errorTasks;
    }

    private List<TaskVO> listTasks(Map<Long, List<TaskVO>> taskMap, String[] taskIdStringArr) {
        List<TaskVO> tasks = Lists.newArrayList();
        for (String taskIdString : taskIdStringArr) {
            Long taskId = Long.valueOf(taskIdString);
            TaskVO task = taskMap.get(taskId).get(0);
            tasks.add(task);
        }
        return tasks;
    }

    private List<TaskVO> listTaskVos(List<Map<String, Object>> targetTaskIds) {
        Set<Long> taskIds = Sets.newHashSet();
        for (Map<String, Object> targetMap : targetTaskIds) {
            String parentId = targetMap.get(PARENT_ID).toString();
            taskIds.add(Long.valueOf(parentId));
            String taskIdsString = targetMap.get(TASK_IDS).toString();
            String[] taskIdStringArr = taskIdsString.split(",");
            for (String taskIdString :  taskIdStringArr) {
                taskIds.add(Long.valueOf(taskIdString));
            }
        }
        return taskService.listAllByIds(Lists.newArrayList(taskIds));
    }

    private PageUtils<Map<String, Object>> listPage(DqcTaskDto dqcTaskDto) {
        Boolean isBlock = dqcTaskDto.getStatus() == null ? null : dqcTaskDto.getStatus().getIsBlock();
        DqcRule dqcRule = new DqcRule();
        dqcRule.setIsBlock(isBlock);
        dqcRule.setDbName(dqcTaskDto.getDbName());
        dqcRule.setTableName(dqcTaskDto.getTableName());
        return dqcRuleService.page(dqcRule);
    }

    @Transactional
    public List<DqcRunVo> run(DqcRunDto dqcRunDto, LoginUserDto userDto) {
        Assert.notNull(dqcRunDto);
        Assert.notNull(dqcRunDto.getDbName());
        Assert.notNull(dqcRunDto.getTaskTime());
        List<DqcRuleVO> dqcRuleVOS = dqcRuleService.list(dqcRunDto.getDbName(), dqcRunDto.getTableName());
        if (CollectionUtils.isEmpty(dqcRuleVOS)) {
            return Lists.newArrayList();
        }
        List<Long> jobIds = dqcRuleVOS.stream().filter(dqc -> dqc.getJobId() != null).map(DqcRuleVO::getJobId).collect(Collectors.toList());
        taskService.delByJobIds(jobIds, true);
        Map<Long, String> map = dqcRuleVOS.stream().filter(dqc -> dqc.getJobId() != null).collect(Collectors.toMap(DqcRuleVO::getJobId, DqcRuleVO::getName, (newValue, oldValue)-> oldValue));
        List<DqcRunVo> dqcRunVos = Lists.newArrayList();
        for (Long jobId : jobIds) {
            Long taskId = jobBizService.addOffTask( dqcRunDto.getTaskTime(), userDto, jobId);
            DqcRunVo dqcRunVo = new DqcRunVo();
            dqcRunVo.setTaskId(taskId);
            dqcRunVo.setRuleName(map.get(jobId));
            dqcRunVos.add(dqcRunVo);
        }
        return dqcRunVos;
    }

    public DqcDetailVo detail(String dbName, String tableName, LoginUserDto userDto) {
        Assert.notNull(dbName);
        Assert.notNull(tableName);

        DqcDetailVo dqcDetailVo = new DqcDetailVo();
        dqcDetailVo.setDbName(dbName);
        dqcDetailVo.setTableName(tableName);

        TableOwnerDto tableOwnerDto = getTableOwnerDto(dbName, tableName);
        if (tableOwnerDto != null) {
            dqcDetailVo.setUserName(tableOwnerDto.getName());
            dqcDetailVo.setUserId(userService.getUserIdByUid(tableOwnerDto.getUid()));
        }

        // 任务信息
        Long relJobId = dqcTableService.getRelJobId(dbName, tableName);
        if (relJobId == null) {
            return dqcDetailVo;
        }
        dqcDetailVo.setJobId(relJobId);
        JobOnline jobOnline = jobOnlineService.getById(relJobId);
        if (jobOnline != null) {
            dqcDetailVo.setJobName(jobOnline.getName());
        }

        return dqcDetailVo;
    }

    public void isOwner(String dbName, String tableName, String uid) {
        Assert.notBlank(dbName);
        Assert.notBlank(tableName);
        Assert.notBlank(uid);

        TableOwnerDto tableOwnerDto = this.getTableOwnerDto(dbName, tableName);
        if (tableOwnerDto == null) {
            throw new BizException(ErrorCode.DQC_TABLE_NOT_OWNER);
        }
        if (!StringUtils.equalsIgnoreCase(tableOwnerDto.getUid(), uid)) {
            throw new BizException(ErrorCode.DQC_TABLE_NOT_PERMISSION);
        }
    }


    @Nullable
    private TableOwnerDto getTableOwnerDto(String dbName, String tableName) {
        String key = DbUtil.getTableKey(DbUtil.HIVE_VALUE, dbName, tableName);
//        BaseResult<Map<String, TableOwnerDto>> baseResult = metaClientService.getUserByTableKey(Lists.newArrayList(key));
//        TableOwnerDto tableOwnerDto = null;
//        if (baseResult.isSuccess().equals(true)) {
//            tableOwnerDto = baseResult.getBody().get(key);
//        }
//        return tableOwnerDto;
        return new TableOwnerDto();
    }

    public Map<String, List<ContentDto>> listEnums() {

        List<ContentDto> contentDtos = Lists.newArrayList();
        for (DqcTaskStatusEnum status : DqcTaskStatusEnum.values()) {
            ContentDto contentDto = new ContentDto();
            contentDto.setDesc(status.getDesc());
            contentDto.setValue(status.name());
            contentDtos.add(contentDto);
        }
        Map<String, List<ContentDto>> map = Maps.newHashMap();
        map.put("dqcTaskStatus", contentDtos);
        return map;
    }
}
