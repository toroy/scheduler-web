package com.clubfactory.platform.scheduler.web.server.tasks;

import com.clubfactory.platform.scheduler.web.core.dto.DataSourceDto;
import com.clubfactory.platform.scheduler.web.core.utils.DataSourceUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * 数据源连接测试
 * @author xiejiajun
 */
@Component(value = "connTestTask")
public class DataSourceConnTestTask {

    /**
     * 进行数据源测试的异步任务
     * @param dto
     * @return
     */
    @Async("dbConnTestExecutor")
    public Future<Boolean> testDatabaseConn(DataSourceDto dto,int timeout){
        boolean connState = DataSourceUtils.testDataSource(dto,timeout);
        return AsyncResult.forValue(connState);
    }
}
