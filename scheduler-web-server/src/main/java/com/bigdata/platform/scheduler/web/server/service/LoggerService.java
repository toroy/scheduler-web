package com.bigdata.platform.scheduler.web.server.service;

import com.bigdata.platform.scheduler.web.core.service.*;
import com.bigdata.platform.scheduler.web.server.login.LoginUserDto;
import com.bigdata.platform.scheduler.web.server.remote.LogClientManager;
import com.bigdata.platform.scheduler.common.bean.tuple.Tuple2;
import com.bigdata.platform.scheduler.common.bean.tuple.Tuple3;
import com.bigdata.platform.scheduler.common.exception.BizException;
import com.bigdata.platform.scheduler.common.util.Assert;
import com.bigdata.platform.scheduler.dal.enums.TaskStatusEnum;
import com.bigdata.platform.scheduler.dal.po.Job;
import com.bigdata.platform.scheduler.dal.po.JobOnline;
import com.bigdata.platform.scheduler.dal.po.LogMap;
import com.bigdata.platform.scheduler.dal.po.Task;
import com.bigdata.platform.scheduler.logger.vo.LogVO;
import com.bigdata.platform.scheduler.web.core.enums.ErrorCode;
import com.bigdata.platform.scheduler.web.core.utils.DateUtils;
import com.bigdata.platform.scheduler.web.core.vo.MachineVO;
import com.bigdata.platform.scheduler.web.core.vo.TaskVO;
import com.bigdata.platform.scheduler.web.server.dqc.service.DqcBizService;
import com.bigdata.platform.scheduler.web.server.dto.BaseLogDto;
import com.bigdata.platform.scheduler.web.server.dto.LastNRowsLogDto;
import com.bigdata.platform.scheduler.web.server.dto.LoggerDto;
import com.bigdata.platform.scheduler.web.server.service.inter.LogReader;
import com.bigdata.platform.scheduler.web.server.vo.LogMapVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xiejiajun
 */
@Service
@Slf4j
public class LoggerService {

    @Value("${task.logger.server.port}")
    private Integer loggerServerPort;
    @Value("${task.log.expire.days}")
    private Integer logExpireDays;
    @Value("${dqc.log-url.base-path}")
    private String dqcLogUrlBasePath;

    @Autowired
    private MachineService machineService;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobOnlineService jobOnlineService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private LogMapService logMapService;
    @Autowired
    private DqcBizService dqcBizService;

    private LogClientManager logClientManager;
    private long logExpireSeconds;

    private final static Integer LOG_SKIP_NUM  = 20;

    @PostConstruct
    public void init() {
        if (loggerServerPort == null) {
            loggerServerPort = 50051;
        }
        if (logExpireDays == null) {
            logExpireDays = 7;
        }
        logExpireSeconds = logExpireDays * 24 * 3600;

        logClientManager = LogClientManager.getInstance();
    }


    public List<LogMapVo> listLogMapInfo(Long taskId) {
    	Tuple2<Long, String> tuple2 = getJob(taskId);
        List<LogMap> logMapList = logMapService.list(taskId);
        int skip = logMapList.size() > LOG_SKIP_NUM ? logMapList.size() - LOG_SKIP_NUM : 0;

        List<LogMapVo> logMapVos = logMapList.stream()
                .map(po -> LogMapVo.builder()
                        .logId(po.getId())
                        .logName(po.getLogName())
                        .logHost(po.getLogHost())
                        .logPath(po.getLogPath())
                        .taskId(taskId)
                        .jobId(tuple2.f0)
                        .jobName(tuple2.f1)
                        .build()
                ).skip(skip).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(logMapVos)){
            throw new BizException("日志信息不存在");
        }
        return logMapVos;
    }


	private Tuple2<Long, String> getJob(Long taskId) {
		TaskVO taskVO = taskService.getById(taskId);
		Tuple2<Long, String> tuple2 = new Tuple2<Long, String>();
    	if (BooleanUtils.isTrue(taskVO.getIsTemp())) {
    		Job job = jobService.getById(taskVO.getJobId());
    		tuple2.setFields(job.getId(), job.getName());
    	} else {
    		JobOnline jobOnline = jobOnlineService.getById(taskVO.getJobId());
    		if (jobOnline != null) {
    			tuple2.setFields(jobOnline.getJobId(), jobOnline.getName());
    		} else {
    			Job job = jobService.getById(taskVO.getJobId());
        		tuple2.setFields(job.getId(), job.getName());
    		}
    	}
		return tuple2;
	}

    public LogVO queryLog(LoggerDto loggerDto) {

        return this.readLogs(loggerDto, logMap -> {
            String host = logMap.getLogHost();
            String logPath = logMap.getLogPath();
            try {
                LogVO logVO = logClientManager.callRemote(host, loggerServerPort, logClient ->
                        logClient.rollViewLogVo(logPath, loggerDto.getOffset(), loggerDto.getSize())
                );
                this.htmlTagEncode(logVO);
                if (loggerDto.getOffset() > 0) {
                    return logVO;
                }
                String dqcRuleLogHrefList = this.generateDqcLogHrefs(logMap);
                if (StringUtils.isBlank(dqcRuleLogHrefList)) {
                    return logVO;
                }
                logVO.setContent(String.format("%s\n%s", dqcRuleLogHrefList, logVO.getContent()));
                return logVO;
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new BizException("拉取日志失败");
            }
        });
    }

    /**
     * 组装DQC规则日志URL
     * @param ruleTaskList
     * @param dqcLogHref
     * @return
     */
    private void buildDqcLogUrl(List<Task> ruleTaskList, StringBuilder dqcLogHref, String title) {
        dqcLogHref.append("<b>").append(title).append(":").append("</b>");
        ruleTaskList.forEach(
                task -> dqcLogHref
                        .append("\t")
                        .append(String.format("<a href='%s/%s' target='_blank'>%s</a>", this.dqcLogUrlBasePath,
                                task.getId(), task.getName()))
        );
        dqcLogHref.append("<br/>");
    }
    /**
     * 生成DQC规则执行日志列表
     * @param logMap
     * @return
     */
    private String generateDqcLogHrefs(LogMap logMap) {
        if (StringUtils.isBlank(dqcLogUrlBasePath)) {
            return "";
        }
        Long taskId = logMap.getTaskId();
        if (taskId == null) {
            return "";
        }
        List<Tuple3<Long, TaskStatusEnum, String>> ruleTaskInfos = this.dqcBizService.listDqcTaskByTaskId(taskId);
        if (CollectionUtils.isEmpty(ruleTaskInfos)) {
            return "";
        }
        List<Task> ruleTaskList = ruleTaskInfos.stream()
                .filter(item -> Objects.nonNull(item) && !item.isNull())
                .map(item -> {
                    Task task = new Task();
                    task.setId(item.f0);
                    task.setStatus(item.f1);
                    task.setName(item.f2);
                    return task;
                }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ruleTaskList)) {
            return "";
        }
        List<Task> runningTaskList = ruleTaskList.stream()
                .filter(task -> TaskStatusEnum.RUNNING == task.getStatus()).collect(Collectors.toList());
        List<Task> successTaskList = ruleTaskList.stream()
                .filter(task -> TaskStatusEnum.SUCCESS == task.getStatus()).collect(Collectors.toList());
        List<Task> failedTaskList = ruleTaskList.stream()
                .filter(task -> TaskStatusEnum.FAILED == task.getStatus() || TaskStatusEnum.DATA_FAILED == task.getStatus())
                .collect(Collectors.toList());
        ruleTaskList.removeAll(runningTaskList);
        ruleTaskList.removeAll(successTaskList);
        ruleTaskList.removeAll(failedTaskList);
        StringBuilder dqcLogHref = new StringBuilder();
        if (CollectionUtils.isNotEmpty(runningTaskList)) {
            this.buildDqcLogUrl(runningTaskList, dqcLogHref, "正在运行的规则");
        }
        if (CollectionUtils.isNotEmpty(successTaskList)) {
            this.buildDqcLogUrl(successTaskList, dqcLogHref, "运行成功的规则");
        }
        if (CollectionUtils.isNotEmpty(failedTaskList)) {
            this.buildDqcLogUrl(failedTaskList, dqcLogHref, "运行失败的规则");
        }
        if (CollectionUtils.isNotEmpty(ruleTaskList)) {
            this.buildDqcLogUrl(ruleTaskList, dqcLogHref, "未运行的规则");
        }
        return dqcLogHref.toString();
    }

    /**
     * 拉取指定实例的最后N行日志
     *
     * @param nRowsLogDto
     * @return
     */
    public LogVO queryLastNRowsLog(LastNRowsLogDto nRowsLogDto) {
        return this.readLogs(nRowsLogDto, logMap -> {
            String host = logMap.getLogHost();
            String logPath = logMap.getLogPath();
            try {
                LogVO logVO =  logClientManager.callRemote(host, loggerServerPort, logClient ->
                        logClient.getLastRowsLogVO(logPath, nRowsLogDto.getRows()));
                this.htmlTagEncode(logVO);
                return logVO;
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new BizException("拉取日志失败");
            }

        });
    }

    /**
     * 根据启动服务的用户名获取所有worker的pubkey
     *
     * @param osUser
     * @return
     */
    public List<String> listAllWorkerPubKey(LoginUserDto userDto, String osUser) {
        if (!userDto.getIsAdmin()) {
            throw new BizException("当前只支持管理员进行该操作...");
        }
        String pubKeyFile = String.format("/home/%s/.ssh/id_rsa.pub", osUser);
        List<MachineVO> activeWorkers = machineService.listActiveWorkers();
        if (CollectionUtils.isEmpty(activeWorkers)) {
            return new ArrayList<>(0);
        }
        List<String> pubKeyList = new ArrayList<>(activeWorkers.size());
        for (MachineVO activeWorker : activeWorkers) {
            try {
                String pubkey = logClientManager.callRemote(activeWorker.getIp(), loggerServerPort,
                        logClient -> logClient.viewLog(pubKeyFile));
                pubKeyList.add(pubkey);
            } catch (Exception e) {
                log.error("pull pubkey failed from {} : {}", activeWorker.getIp(), e.getMessage());
            }
        }
        return pubKeyList;
    }


    /**
     * 检查日志是否已经过期
     *
     * @param logMap
     * @return
     */
    private boolean logIsExpired(LogMap logMap) {
        long taskLaunchTime = DateUtils.getStartTimeOfDay(logMap.getUpdateTime());
        long currentTime = DateUtils.getStartTimeOfDay(new Date());
        return (currentTime - taskLaunchTime) > this.logExpireSeconds;
    }

    /**
     * 读取日志
     *
     * @return
     */
    private LogVO readLogs(BaseLogDto logDto, LogReader logReader) {
        Assert.notNull(logDto);
        Assert.notNull(logDto.getLogId(),"日志ID");

        LogMap logMap = logMapService.getById(logDto.getLogId());
        if (logMap == null) {
            throw new BizException(ErrorCode.TASK_LOG_NOT_EXISTS);
        }
        if (StringUtils.isEmpty(logMap.getLogHost())) {
            throw new BizException(ErrorCode.TASK_LOG_HOST_IS_NULL);
        }
        if (StringUtils.isEmpty(logMap.getLogPath())) {
            throw new BizException(ErrorCode.TASK_LOG_PATH_IS_NULL);
        }

        if (logIsExpired(logMap)) {
            return LogVO.builder()
                    .offset(0)
                    .content(String.format("当前只支持查询【%s】天内执行的任务日志", logExpireDays))
                    .build();
        }
        return logReader.read(logMap);
    }

    @PreDestroy
    public void destroy() {
        logClientManager.close();
    }

    /**
     * Html标签转义
     * @param logVO
     */
    private void htmlTagEncode(LogVO logVO) {
        if (Objects.isNull(logVO)) {
            return;
        }
        String content = logVO.getContent();
        if (StringUtils.isBlank(content)) {
            return;
        }
        content = StringUtils.replace(content, ">", "&gt;");
        content = StringUtils.replace(content, "<", "&lt;");
        logVO.setContent(content);
    }

}
