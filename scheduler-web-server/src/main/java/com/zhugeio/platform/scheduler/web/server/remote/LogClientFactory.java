package com.zhugeio.platform.scheduler.web.server.remote;

import com.zhugeio.platform.scheduler.logger.LogClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author xiejiajun
 */
@Slf4j
public class LogClientFactory extends BasePooledObjectFactory<LogClient> {

    private String host;

    private Integer port;

    LogClientFactory(String host, int port) {
        this.host = host;
        this.port = port;
    }


    @Override
    public LogClient create() throws Exception {
        return new LogClient(host,port);
    }

    @Override
    public PooledObject<LogClient> wrap(LogClient logClient) {
        return new DefaultPooledObject<>(logClient);
    }

    @Override
    public void destroyObject(PooledObject<LogClient> p) {
        try {
            p.getObject().shutdown();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public boolean validateObject(PooledObject<LogClient> p) {
        return true;
    }
}
