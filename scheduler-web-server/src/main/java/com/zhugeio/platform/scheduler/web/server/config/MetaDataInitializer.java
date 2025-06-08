package com.zhugeio.platform.scheduler.web.server.config;

import com.zhugeio.platform.scheduler.web.server.service.TeamBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiejiajun
 */
@Configuration
public class MetaDataInitializer {

    @Autowired
    private TeamBizService teamBizService;

    /**
     * 服务启动时初始化sc_team表
     */
    //@PostConstruct
    public void initTeamInfo(){
        if (teamBizService.teamInfosIsEmpty()){
            teamBizService.refreshTeams();
        }
    }
}
