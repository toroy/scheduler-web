package com.bigdata.platform.scheduler.web.server.remote;

import com.bigdata.platform.scheduler.logger.LogClient;
import com.bigdata.platform.scheduler.web.core.utils.ThreadUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author xiejiajun
 */
@Slf4j
public class LogClientManager {

    private static LogClientManager instance = new LogClientManager();

    private final Map<String, GenericObjectPool<LogClient>> clientPoolMap = Maps.newConcurrentMap();
    private final Map<String, Boolean> rebuildPoolMap = Maps.newConcurrentMap();
    private boolean stopped = false;
    private ExecutorService poolMonitor;

    private LogClientManager() {
        this.poolMonitor = ThreadUtils.newDaemonSingleThreadExecutor("log_client-pool-monitor");
        this.poolMonitor.submit(() -> {
            while (!stopped) {
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(5));
                    if (rebuildPoolMap.size() == 0) {
                        continue;
                    }
                    Iterator<Map.Entry<String, Boolean>> itor = rebuildPoolMap.entrySet().iterator();
                    while (itor.hasNext()) {
                        Map.Entry<String, Boolean> rebuildInfo = itor.next();
                        Boolean needRebuild = rebuildInfo.getValue();
                        if (needRebuild != null && needRebuild) {
                            String host = rebuildInfo.getKey();
                            this.destroyPool(host);
                            itor.remove();
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        });
    }

    public static LogClientManager getInstance() {
        return instance;
    }

    /**
     * 获取客户端
     *
     * @param host
     * @param port
     * @return
     */
    private LogClient getClient(String host, Integer port) throws Exception {
        GenericObjectPool<LogClient> clientPool = this.validateOrCreatePool(host, port, clientPoolMap.get(host));
        ;
        try {
            return clientPool.borrowObject();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        } finally {
            int activeNum = clientPool.getNumActive();
            int idleNum = clientPool.getNumIdle();
            int waiterNum = clientPool.getNumWaiters();
            log.info("申请连接后, Host:{}对应的连接池总连接数:{}, 空闲连接数:{}, 活跃连接数: {}, 排队请求数: {}", host, (activeNum + idleNum),
                    idleNum, activeNum, waiterNum);
        }

    }

    /**
     * 销毁有问题的客户端并重新创建
     *
     * @param host
     * @param port
     * @param client
     * @return
     * @throws Exception
     */
    private LogClient releaseAndNewClient(String host, Integer port, LogClient client) throws Exception {
        this.releaseBrokenClient(host, client, null);
        return this.getClient(host, port);
    }

    /**
     * 销毁或回收客户端
     *
     * @param host
     * @param client
     * @param broken
     */
    public void releaseClient(String host, LogClient client, boolean broken) {
        GenericObjectPool<LogClient> clientPool = null;
        try {
            clientPool = clientPoolMap.get(host);
            if (broken) {
                releaseBrokenClient(host, client, clientPool);
            } else {
                try {
                    if (client == null) {
                        return;
                    }
                    if (clientPool != null) {
                        clientPool.returnObject(client);
                    } else {
                        client.shutdown();
                    }
                } catch (Exception e) {
                    log.warn("exception occurred during releasing thrift client", e);
                }
            }
        } finally {
            if (clientPool != null) {
                int activeNum = clientPool.getNumActive();
                int idleNum = clientPool.getNumIdle();
                int waiterNum = clientPool.getNumWaiters();
                log.info("归还连接后，Host:{}对应的连接池总连接数:{}, 空闲连接数:{}, 活跃连接数: {}, 排队请求数: {}", host, (activeNum + idleNum),
                        idleNum, activeNum, waiterNum);
            }
        }
    }

    /**
     * 销毁客户端
     *
     * @param host
     * @param client
     * @param clientPool
     */
    private void releaseBrokenClient(String host, LogClient client, GenericObjectPool<LogClient> clientPool) {
        try {
            if (client == null) {
                return;
            }
            if (clientPool == null) {
                clientPool = clientPoolMap.get(host);
            }
            if (clientPool != null) {
                clientPool.invalidateObject(client);
            } else {
                client.shutdown();
            }
        } catch (Exception e) {
            log.warn("exception occurred during releasing thrift client", e);
        }
    }

    /**
     * @param host
     * @param port
     * @param clientPool
     * @return
     */
    private GenericObjectPool<LogClient> validateOrCreatePool(String host, Integer port,
                                                              GenericObjectPool<LogClient> clientPool) {
        if (clientPool == null || clientPool.isClosed()) {
            synchronized (this) {
                GenericObjectPool<LogClient> newClientPool = clientPoolMap.get(host);
                if (newClientPool == null || newClientPool.isClosed()) {
                    clientPool = new GenericObjectPool<>(new LogClientFactory(host, port));
                    clientPool.setMaxIdle(10);
                    clientPool.setMinIdle(1);
                    clientPool.setMaxTotal(50);
                    clientPool.setMaxWaitMillis(2000);
                    clientPool.setAbandonedConfig(new AbandonedConfig());
                    clientPoolMap.put(host, clientPool);
                } else {
                    clientPool = newClientPool;
                }
            }
        }
        return clientPool;
    }

    /**
     * 重建有问题的客户端连接池
     *
     * @param host
     */
    private void rebuildPool(String host) {
        if (host == null) {
            return;
        }
        this.rebuildPoolMap.put(host, true);
    }

    /**
     * 销毁连接池
     *
     * @param host
     */
    private void destroyPool(String host) {
        GenericObjectPool<LogClient> clientPool = clientPoolMap.get(host);
        if (clientPool == null) {
            return;
        }
        try {
            clientPool.close();
        } catch (Exception e) {
            log.error("销毁问题线程池失败: {}", e.getMessage());
        } finally {
            clientPoolMap.remove(host, clientPool);
        }
    }

    /**
     * 关闭连接池监控线程
     */
    public void close() {
        this.stopped = true;
        if (this.poolMonitor != null && !this.poolMonitor.isTerminated() && !this.poolMonitor.isShutdown()) {
            this.poolMonitor.shutdownNow();
        }
    }

    /**
     * 获取日志客户端：封装了重试逻辑
     *
     * @param host
     * @param port
     * @return
     */
    public LogClient borrowClient(String host, Integer port) {
        LogClient logClient = null;
        try {
            try {
                logClient = this.getClient(host, port);
            } catch (Exception e) {
                logClient = this.releaseAndNewClient(host, port, logClient);
            }
        } catch (Exception e) {
            this.rebuildPool(host);
        }
        return logClient;
    }


    /**
     * 远程调用
     *
     * @param host
     * @param port
     * @param function
     * @param destroyClient
     * @return
     */
    public <T> T callRemote(String host, Integer port, RemoteFunction<T> function, boolean destroyClient) throws Exception {
        LogClient logClient = null;
        T ret = null;
        try {
            try {
                logClient = this.getClient(host, port);
                ret = function.call(logClient);
            } catch (Exception e) {
                logClient = this.releaseAndNewClient(host, port, logClient);
                ret = function.call(logClient);
            }
        } catch (Exception e) {
            if (logClient == null) {
                this.rebuildPool(host);
            } else {
                this.releaseClient(host, logClient, true);
            }
            throw e;
        }
        if (ret != null) {
            this.releaseClient(host, logClient, destroyClient);
        }
        return ret;
    }

    /**
     * 远程调用
     *
     * @param host
     * @param port
     * @param function
     * @return
     */
    public <T> T callRemote(String host, Integer port, RemoteFunction<T> function) throws Exception {
        return this.callRemote(host, port, function, false);
    }

    /**
     * 无返回值调用
     *
     * @param host
     * @param port
     * @param function
     * @param destroyClient
     */
    public void runRemote(String host, Integer port, NoRetFunction function, boolean destroyClient) throws Exception {
        LogClient logClient = null;
        try {
            try {
                logClient = this.getClient(host, port);
                function.run(logClient);
            } catch (Exception e) {
                logClient = this.releaseAndNewClient(host, port, logClient);
                function.run(logClient);
            }
        } catch (Exception e) {
            if (logClient == null) {
                this.rebuildPool(host);
            } else {
                this.releaseClient(host, logClient, true);
            }
            throw e;
        }
        this.releaseClient(host, logClient, destroyClient);
    }

    /**
     * 无返回值调用
     *
     * @param host
     * @param port
     * @param function
     */
    public void runRemote(String host, Integer port, NoRetFunction function) throws Exception {
        this.runRemote(host, port, function, false);
    }


    public interface RemoteFunction<T> {
        /**
         * 远程调用
         *
         * @param logClient
         * @return
         */
        T call(LogClient logClient);
    }

    /**
     * 无返回值函数
     *
     * @param
     */
    public interface NoRetFunction {
        /**
         * 远程调用
         *
         * @param logClient
         */
        void run(LogClient logClient);
    }

}
