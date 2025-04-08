package com.clubfactory.platform.scheduler.web.server.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import com.clubfactory.platform.scheduler.common.enums.ResourceType;
import com.clubfactory.platform.scheduler.web.core.service.LockService;
import com.clubfactory.platform.scheduler.web.server.service.TeamBizService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xiejiajun
 */
@Slf4j
@Configuration
public class PollTeamListTask {

    @Autowired
    private TeamBizService teamBizService;

    @Autowired
    private LockService lockService;


    @Async("taskExecutor")
    @Scheduled(cron = "${team.list.poll.cron}")
    public void refreshTaskList(){
        if (lockService.tryLockOnce(ResourceType.TEAM_POLLER_LOCK,600)) {
            log.info("当前节点获取团队信息更新任务锁成功,开始更新团队信息");
            try {
                teamBizService.refreshTeams();
            }finally {
                lockService.unlock(ResourceType.TEAM_POLLER_LOCK);
            }

        }else {
            log.info("当前节点获取团队信息更新任务锁失败,跳过本次团队信息更新");
        }
    }
}
