package com.bigdata.platform.scheduler.web.server.tasks;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bigdata.platform.scheduler.common.enums.ResourceType;
import com.bigdata.platform.scheduler.web.core.service.LockService;
import com.bigdata.platform.scheduler.web.core.service.MachineService;
import com.bigdata.platform.scheduler.web.core.vo.MachineVO;
import com.bigdata.platform.scheduler.web.server.remote.LogClientManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 过期日志清理任务
 * @author xiejiajun
 */
@Slf4j
@Component
public class ExpireLogCleanTask {

    @Value("${task.logger.server.port}")
    private Integer loggerServerPort;

    @Value("${task.log.expire.days}")
    private Integer logExpireDays;

    @Autowired
    private MachineService machineService;

    @Autowired
    private LockService lockService;

    private LogClientManager logClientManager;


    @PostConstruct
    public void init(){
        if (loggerServerPort == null){
            loggerServerPort = 50051;
        }
        if (logExpireDays == null){
            logExpireDays = 7;
        }
        logClientManager = LogClientManager.getInstance();
    }

    @Scheduled(cron = "${task.log.expire.clear.cron}")
    public void clearExpireLog(){
        if (lockService.tryLockOnce(ResourceType.LOG_CLEANER_LOCK,600)) {
            try {
                log.info("当前节点竞争日志清理任务锁成功,开始执行日志清理任务");
                List<MachineVO> workers = machineService.listActiveWorkers();
                if (CollectionUtils.isEmpty(workers)) {
                    return;
                }
                for (MachineVO machineVO : workers) {
                    if (StringUtils.isBlank(machineVO.getIp())) {
                        continue;
                    }
                    String host = machineVO.getIp();
                    try {
                        logClientManager.runRemote(host,loggerServerPort, logClient ->
                                logClient.clearExpireLog(logExpireDays));
                    }catch (Exception e){
                        log.error("日志清理失败:{}",e.getMessage());
                    }

                }
            }finally {
                lockService.unlock(ResourceType.LOG_CLEANER_LOCK);
            }

        }else {
            log.info("当前节点竞争日志清理任务锁失败,跳过日志清理任务");
        }

    }
}
