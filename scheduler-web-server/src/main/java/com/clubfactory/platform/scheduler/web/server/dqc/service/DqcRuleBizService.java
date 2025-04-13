package com.clubfactory.platform.scheduler.web.server.dqc.service;

import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.common.util.Assert;
import com.clubfactory.platform.scheduler.common.util.BeanUtil;
import com.clubfactory.platform.meta.client.dto.ColumnDto;
import com.clubfactory.platform.meta.client.dto.TableOwnerDto;
import com.clubfactory.platform.meta.client.utils.DbUtil;
import com.clubfactory.platform.scheduler.dal.dto.SchedulerTimeDto;
import com.clubfactory.platform.scheduler.dal.enums.*;
import com.clubfactory.platform.scheduler.dal.po.*;
import com.clubfactory.platform.scheduler.web.core.constant.SysConfigConstant;
import com.clubfactory.platform.scheduler.web.core.dqc.service.DqcPartitionService;
import com.clubfactory.platform.scheduler.web.core.dqc.service.DqcRuleService;
import com.clubfactory.platform.scheduler.web.core.dqc.service.DqcTableService;
import com.clubfactory.platform.scheduler.web.core.dqc.vo.DqcRuleVO;
import com.clubfactory.platform.scheduler.web.core.dto.JobCalDto;
import com.clubfactory.platform.scheduler.web.core.enums.ErrorCode;
import com.clubfactory.platform.scheduler.web.core.proxy.MetaClientProxy;
import com.clubfactory.platform.scheduler.web.core.service.*;
import com.clubfactory.platform.scheduler.web.core.utils.SysConfigUtil;
import com.clubfactory.platform.scheduler.web.server.dqc.dto.ContentDto;
import com.clubfactory.platform.scheduler.web.server.dqc.dto.RuleEnumDto;
import com.clubfactory.platform.scheduler.web.server.login.LoginUserDto;
import com.clubfactory.platform.scheduler.web.server.service.JobCheckBizService;
import com.clubfactory.platform.scheduler.web.server.service.JobDetailBizService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DqcRuleBizService {

    @Resource
    private MetaClientProxy metaClientProxy;
    @Resource
    private DqcRuleService dqcRuleService;
    @Resource
    private DqcPartitionService dqcPartitionService;
    @Resource
    private JobDetailBizService jobDetailBizService;
    @Resource
    private JobCheckBizService jobCheckBizService;
    @Resource
    private TaskService taskService;
    @Resource
    private TaskDependsService taskDependsService;
    @Resource
    private JobService jobService;
    @Resource
    private UserService userService;
    @Resource
    private JobOnlineService jobOnlineService;
    @Resource
    private DqcTableService dqcTableService;
    @Resource
    private AlarmService alarmService;
    @Resource
    private DqcBizService dqcBizService;

    private final static String TABLE_VALUE = "TABLE";
    private final static String TABLE_DESC = "表级规则";
    private String DQC_JOB_NAME  = "dqc_%s_%s";
    private final static Integer RULE_NUM_MAX = 6;
    private final static String DD = "dd";
    private final static String AND = "and";

    public Map<String, Object> listEnums(String dbName, String tableName) {

        List<ColumnDto> columnDtoList = metaClientProxy.listHiveColumns(dbName, tableName);
        RuleEnumDto ruleEnumDto = new RuleEnumDto();

        List<ContentDto> columns = listEnums(columnDtoList);
        columns.add(0, new ContentDto(TABLE_DESC, TABLE_VALUE));
        ruleEnumDto.setFields(columns);

        // 采样方式
        ruleEnumDto.setSamples(listEnums(SampleEnum.values()));
        // 校验类型
        ruleEnumDto.setCheckTypes(listEnums(CheckTypeEnum.values()));

        // 链接方式
        ruleEnumDto.setConnectorTypes(listEnums(ConnectorTypeEnum.values()));

        // 校验方式
        RuleEnumDto.CheckTypeEnumDto checkModeEnumDto = new RuleEnumDto.CheckTypeEnumDto();
        checkModeEnumDto.setNUMBER(listEnums(CheckModeEnum.values(), CheckTypeEnum.NUMBER));
        checkModeEnumDto.setROLLING(listEnums(CheckModeEnum.values(), CheckTypeEnum.ROLLING));
        ruleEnumDto.setCheckModes(checkModeEnumDto);

        // 比较方式
        RuleEnumDto.CheckTypeEnumDto comparesDto = new RuleEnumDto.CheckTypeEnumDto();
        comparesDto.setNUMBER(listEnums(CompareEnum.values(), CheckTypeEnum.NUMBER));
        comparesDto.setROLLING(listEnums(CompareEnum.values(), CheckTypeEnum.ROLLING));
        ruleEnumDto.setCompares(comparesDto);

        return BeanUtil.copyBean2Map(ruleEnumDto, Maps.newHashMap());
    }

    @Transactional
    public Boolean save(DqcRule dqcRule, LoginUserDto userDto) {
        Assert.notNull(dqcRule);
        Assert.notNull(dqcRule.getTableName());
        Assert.notNull(dqcRule.getName());
        Assert.notNull(dqcRule.getDbName());

        // 限制检查
        check(dqcRule, userDto);

        Long relJobId = dqcTableService.getRelJobId(dqcRule.getDbName(), dqcRule.getTableName());
        // 新建规则
        dqcRule.setRelJobId(relJobId);
        dqcRule.setCreateUser(userDto.getLocalUserId());
        dqcRule.setUpdateUser(userDto.getLocalUserId());
        dqcRule = dqcRuleService.save(dqcRule);

        // 保存任务
        Long jobId = saveJob(dqcRule, userDto);

        // 更新dqc的任务id
        dqcRuleService.updateJobId(dqcRule.getId(), jobId);

        if (relJobId != null) {
            // 更改调度时间，周期
            JobOnline jobOnline = jobOnlineService.getById(relJobId);
            if (jobOnline == null) {
                return true;
            }
            // 更改dqc的调度时间
            updateJobScheduler(relJobId, Lists.newArrayList(jobId), jobOnline.getSchedulerTimeDto(), jobOnline.getCycleType(), userDto);

            // 生成报警信息
            saveAlarms(userDto, jobId, relJobId);
        }
        return true;
    }

    /**
     * 更改调度时间，dqc任务重新上线
     *
     * @param relJobId
     * @param dqcJobIds
     * @param timeDto
     * @param jobCycleType
     * @param userDto
     * @return
     */
    public List<Long> updateJobScheduler(Long relJobId, List<Long> dqcJobIds, SchedulerTimeDto timeDto, JobCycleTypeEnum jobCycleType, LoginUserDto userDto) {
        if (CollectionUtils.isEmpty(dqcJobIds)) {
            dqcJobIds = dqcRuleService.listByRelJobIds(Lists.newArrayList(relJobId));
        }
        if (CollectionUtils.isEmpty(dqcJobIds)) {
            return dqcJobIds;
        }
        jobService.editScheduler(dqcJobIds, timeDto, jobCycleType);
        // 如果有关联则修改线上任务
        userDto.setIsAdmin(true);
        jobCheckBizService.editSuccessByJobIds(dqcJobIds, false, userDto);
        return dqcJobIds;
    }

    public void editAlarm(Long relJobId, LoginUserDto userDto) {
        List<Long> dqcJobIds = dqcRuleService.listByRelJobIds(Lists.newArrayList(relJobId));
        if (CollectionUtils.isEmpty(dqcJobIds)) {
            return;
        }
        for (Long dqcJobId : dqcJobIds) {
            alarmService.del(dqcJobId);
            this.saveAlarms(userDto, dqcJobId, relJobId);
        }
    }

    private void check(DqcRule dqcRule, LoginUserDto userDto) {
        dqcBizService.isOwner(dqcRule.getDbName(), dqcRule.getTableName(), userDto.getUserid());

        Integer size = Optional.ofNullable(dqcRuleService.list(dqcRule.getDbName(), dqcRule.getTableName())).orElse(Lists.newArrayList()).size();
        if (size > RULE_NUM_MAX) {
            throw new BizException(ErrorCode.DQC_RULE_MAX_NUM_ERROR.setParams(size.toString()));
        }
    }

    private Long saveJob(DqcRule dqcRule, LoginUserDto userDto) {
        // 根据规则找脚本id
        Long scriptId = getScriptId(dqcRule);
        // 获取分区表达式
        String expression = dqcPartitionService.getExpression(dqcRule.getDqcPartitionId());
        // 新建job
        JobCalDto jobDto = genJobCalDto(dqcRule, scriptId, expression);
        return jobDetailBizService.saveCal(jobDto, userDto);
    }

    public List<Long> offRelJob(List<Long> relJobIds) {
        Assert.collectionNonEmpty(relJobIds, "关联任务id");
        List<Long> jobIds = dqcRuleService.listByRelJobIds(relJobIds);
        if (CollectionUtils.isEmpty(jobIds)) {
            return Lists.newArrayList();
        }

        dqcRuleService.cleanRelJobIds(relJobIds);
        dqcTableService.deleteByRelJobIds(relJobIds);
        return jobIds;
    }

    public void saveAlarms(LoginUserDto userDto, Long jobId, Long relJobId) {
        Assert.notNull(jobId);
        Assert.notNull(relJobId);

        List<Alarm> alarms = alarmService.listByJobId(relJobId);
        if (CollectionUtils.isEmpty(alarms)) {
            return;
        }
        // 只复制失败报警的信息
        List<Alarm> alarmsDto = Lists.newArrayList();
        for (Alarm alarm : alarms) {
            if (AlarmTypeEnum.FAILED != alarm.getType()) {
                continue;
            }
            Alarm alarmDto = new Alarm();
            BeanUtil.copyBeanNotNull2Bean(alarm, alarmDto);
            alarmDto.setJobId(jobId);
            alarmDto.setCreateUser(userDto.getLocalUserId());
            alarmDto.setUpdateUser(userDto.getLocalUserId());
            alarmDto.setType(AlarmTypeEnum.FAILED);
            alarmsDto.add(alarmDto);
        }
        alarmService.saveBatch(alarmsDto);
    }

    @NotNull
    private JobCalDto genJobCalDto(DqcRule dqcRule, Long scriptId, String expression) {
        JobCalDto jobDto = new JobCalDto();
        jobDto.setJobType(JobTypeEnum.DQC);
        jobDto.setScriptId(scriptId);
        jobDto.setDbTargetId(SysConfigUtil.getLongByKey(SysConfigConstant.MQ_HIVE_DB_ID));
        jobDto.setDbTargetName(dqcRule.getTableName());
        jobDto.setMachineId(0L);
        jobDto.setIsRunning(false);
        jobDto.setType(ProgramTypeEnum.PYTHON.name());
        jobDto.setTargetTable(String.format("%s.%s", dqcRule.getDbName(), dqcRule.getTableName()));

        StringBuilder sb = genParamBuilder(dqcRule, expression);

        jobDto.setArgsParam(sb.toString());
        jobDto.setCategroy(JobCategoryEnum.CAL);
        jobDto.setIsRunning(false);
        jobDto.setRunOnTmpEmr(false);
        jobDto.setStatus(JobStatusEnum.CHECK);
        jobDto.setRetryMax(3);
        jobDto.setRetryDur(5);
        jobDto.setPriority(0);
        jobDto.setName(String.format(DQC_JOB_NAME, dqcRule.getId(), dqcRule.getName()));
        jobDto.setId(dqcRule.getJobId());
        return jobDto;
    }

    @NotNull
    private StringBuilder genParamBuilder(DqcRule dqcRule, String expression) {
        StringBuilder sb = new StringBuilder();
        // hive 地址
        if (dqcRule.getConnectorType() != null && ConnectorTypeEnum.HIVE == dqcRule.getConnectorType()) {
            sb.append(" -hive_host=${HIVE_HOSTS} ");
        } else {
            sb.append(" -presto_host=${PRESTO_HOST} ");
        }
        // 比较运算符
        sb.append(" -compare_op=");
        sb.append(getString(dqcRule.getCompare().getSymbol()));
        // 库.表名
        sb.append(" -table_name=");
        sb.append(dqcRule.getDbName());
        sb.append(".");
        sb.append(dqcRule.getTableName());
        // 自定义SQL
        if (StringUtils.isNotBlank(dqcRule.getUserSql())) {
            sb.append(" -user_sql=");
            String sql = StringUtils.replaceIgnoreCase(dqcRule.getUserSql(), "'", "\"");
            sb.append(getString(sql));
        }
        // 过滤条件
        if (StringUtils.isNotBlank(dqcRule.getFilter())) {
            sb.append(" -filter_expr=");
            String filter = StringUtils.replaceIgnoreCase(dqcRule.getFilter(), "'", "\"");
            sb.append(getString(filter));
        }

        // 聚会类型
        if (StringUtils.isNotBlank(dqcRule.getField())) {
            sb.append(" -aggr_field=");
            sb.append(StringUtils.equalsIgnoreCase(dqcRule.getField(), "TABLE") ? getString("*") : getString(dqcRule.getField()));
        }
        // 操作符
        if (null != dqcRule.getSample()) {
            sb.append(" -aggr_op=");
            sb.append(dqcRule.getSample().name());
        }
        if (CheckTypeEnum.NUMBER == dqcRule.getCheckType()) {
            sb.append(" -pt_expr=");
            String expressionValue = StringUtils.replaceIgnoreCase(expression, "'", "\"");
            sb.append(getString(expressionValue));
            sb.append(" -bound_val=");
            sb.append(dqcRule.getExpectValue());
        } else if (CheckTypeEnum.ROLLING == dqcRule.getCheckType()) {
            sb.append(" -lower_bound_val=");
            sb.append(dqcRule.getLowerThreshold());
            sb.append(" -upper_bound_val=");
            sb.append(dqcRule.getUpperThreshold());
            sb.append(" -validate_type=");
            sb.append(dqcRule.getCheckMode().getType());

            String[] ptArr = expression.split("=");
            if (ptArr.length == 2) {
                sb.append(" -pt_field=");
                sb.append(getString(ptArr[0]));

                sb.append(" -curr_pt=");
                String expressionValue = replaceSymbol(ptArr[1]);
                sb.append(getString(expressionValue));

                String ptStart = getPtStart(expressionValue, dqcRule.getCheckMode().getDay());
                sb.append(" -pt_start=");
                if (StringUtils.isNotBlank(ptStart)) {
                    sb.append(getString(ptStart));
                }

            }

        }
        return sb;
    }

    @Nullable
    private String replaceSymbol(String text) {
        String expressionValue = StringUtils.replaceIgnoreCase(text, "\"", "");
        expressionValue = StringUtils.replaceIgnoreCase(expressionValue, "\'", "");
        return expressionValue;
    }

    public String getPtStart(String expression, Integer num) {
        if (StringUtils.isBlank(expression)) {
            return null;
        }

        if (StringUtils.containsIgnoreCase(expression, AND)) {
            String[] arr = expression.split(AND);
            for (String value : arr) {
                if (StringUtils.containsIgnoreCase(value, DD)) {
                    return getPtStartValue(expression, num);
                }
                if (StringUtils.containsIgnoreCase(value,"${DT}")) {
                    expression = "$[yyyy-MM-dd]";
                    return getPtStartValue(expression, num);
                }
            }
        } else if (StringUtils.containsIgnoreCase(expression,"${DT}")) {
            expression = "$[yyyy-MM-dd]";
            return getPtStartValue(expression, num);
        } else {
            return getPtStartValue(expression, num);
        }
        return null;
    }

    @Nullable
    private String getPtStartValue(String expression, Integer num) {
        String expressionValue = StringUtils.substringBetween(expression, "[", "]");
        if (StringUtils.containsIgnoreCase(expressionValue, DD)) {
            int ddNum = StringUtils.indexOf(expressionValue.toLowerCase(), DD);
            String value = StringUtils.left(expressionValue,ddNum+2);
            StringBuilder sb = new StringBuilder();
            sb.append("$[");
            sb.append(value);
            sb.append("-");
            sb.append(num);
            sb.append("]");
            return sb.toString();
        }
        return null;
    }

    public static void main(String[] args) {
        DqcRuleBizService service = new DqcRuleBizService();
        String expression = "dt=\"$[YYYY-MM]\"";
        String test = service.getPtStart(expression, 8);
        System.out.println(test);
    }

    private String getString(String val) {
        return "'"+val+"'";
    }


    private Long getScriptId(DqcRule dqcRule) {
        String scriptConfigName = CheckTypeEnum.NUMBER == dqcRule.getCheckType() ? SysConfigConstant.DQC_NUMBER_SCRIPT_ID : SysConfigConstant.DQC_ROLLING_SCRIPT_ID;
        return SysConfigUtil.getLongByKey(scriptConfigName);
    }

    @Transactional
    public Boolean edit(DqcRule dqcRule, LoginUserDto userDto) {
        Assert.notNull(dqcRule);
        Assert.notNull(dqcRule.getId());

        dqcBizService.isOwner(dqcRule.getDbName(), dqcRule.getTableName(), userDto.getUserid());
        // 1. 前一步已经鉴权过，跳过根据身份鉴权
        userDto.setIsAdmin(true);
        // 2. 修改规则
        dqcRuleService.updateDqcRule(dqcRule, userDto.getLocalUserId());
        // 3. 修改任务
        DqcRule dqcRuleRes = dqcRuleService.getById(dqcRule.getId());
        editDqcJob(dqcRule, userDto, dqcRuleRes);
        // 4. 如果有关联则修改线上任务
        if (dqcRuleRes.getRelJobId() != null) {
            jobService.editStatus(Lists.newArrayList(dqcRuleRes.getJobId()), JobStatusEnum.CHECK, userDto.getLocalUserId());
            jobCheckBizService.editSuccessByJobIds(Lists.newArrayList(dqcRuleRes.getJobId()), false, userDto);
        }

        return true;
    }

    private void editDqcJob(DqcRule dqcRule, LoginUserDto userDto, DqcRule dqcRuleRes) {
        // 2.1 根据规则找脚本id
        Long scriptId = getScriptId(dqcRule);
        // 2.2 获取分区表达式
        String expression = dqcPartitionService.getExpression(dqcRuleRes.getDqcPartitionId());
        // 2.3 修改job
        Job job = jobService.getById(dqcRule.getJobId());
        JobCalDto jobDto = genJobCalDto(dqcRule, scriptId, expression);
        jobDto.setSchedulerTime(job.getSchedulerTime());
        jobDto.setId(dqcRuleRes.getJobId());
        jobDetailBizService.editCal(jobDto, userDto);
    }


    public Boolean del(Long id, LoginUserDto userDto) {
        Assert.notNull(id);
        DqcRule dqcRule = dqcRuleService.getById(id);
        if (dqcRule == null) {
            return false;
        }

        // 删dqc任务
        Long jobId = dqcRule.getJobId();
        jobService.del(jobId);
        jobOnlineService.deleteByJobIds(Lists.newArrayList(jobId));

        // 删实例相关
        List<Task> tasks = taskService.listByJobId(jobId);
        if (CollectionUtils.isNotEmpty(tasks)) {
            // 删依赖
            List<Long> taskIds = tasks.stream().map(Task::getId).collect(Collectors.toList());
            taskDependsService.removeByTaskIds(taskIds);
            // 删实例
            taskService.delByIds(taskIds);
        }

        // 删dqc
        dqcRuleService.deleteById(id, userDto.getLocalUserId());

        return true;
    }

    public List<DqcRuleVO> list(String dbName, String tableName, LoginUserDto userDto) {
        Assert.notBlank(dbName);
        Assert.notBlank(tableName);

        // 获取规则列表数据
        List<DqcRuleVO> vos = dqcRuleService.list(dbName, tableName);
        if (CollectionUtils.isEmpty(vos)) {
            return Lists.newArrayList();
        }

        // 获取owner
        String key = DbUtil.getTableKey(DbUtil.HIVE_VALUE, dbName, tableName);
        Map<String, TableOwnerDto> tableOwnerDtoMap = metaClientProxy.getUserByTableKey(key);

        // 获取分区表达式
        List<Long> ids = vos.stream().map(DqcRuleVO::getDqcPartitionId).collect(Collectors.toList());
        Map<Long, String> expressionMap = dqcPartitionService.getExpressionMap(ids);


        vos.stream().forEach(vo -> {
            if (tableOwnerDtoMap.get(key) != null) {
                String uid = tableOwnerDtoMap.get(key).getUid();
                Long userId = userService.getUserIdByUid(uid);
                vo.setUserId(userId);
            }
            vo.setExpression(expressionMap.get(vo.getDqcPartitionId()));
            vo.setUserName(userService.getUserName(vo.getCreateUser()));
        });

        return vos;
    }

    private List<ContentDto> listEnums(List<ColumnDto> columnDtos) {
        if (CollectionUtils.isEmpty(columnDtos)) {
            return  Lists.newArrayList();
        }
        return Lists.newArrayList(columnDtos).stream().map(column -> {
            ContentDto content = new ContentDto();
            content.setValue(column.getName());
            content.setDesc(String.format("%s[%s]", column.getName(), column.getType()));
            return content;
        }).collect(Collectors.toList());
    }

    private List<ContentDto> listEnums(IEnum[] values) {
        return Lists.newArrayList(values).stream().map(typeEnum -> {
            ContentDto content = new ContentDto();
            content.setValue(typeEnum.name());
            content.setDesc(typeEnum.getDesc());
            return content;
        }).collect(Collectors.toList());
    }

    private List<ContentDto> listEnums(ICheckTypeEnum[] values, CheckTypeEnum checkTypeEnum) {
        return Lists.newArrayList(values).stream().filter(typeEnum -> typeEnum.getCheckTypeEnum() == checkTypeEnum).map(typeEnum -> {
            ContentDto content = new ContentDto();
            content.setValue(typeEnum.name());
            content.setDesc(typeEnum.getDesc());
            return content;
        }).collect(Collectors.toList());
    }
}
