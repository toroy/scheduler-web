package com.bigdata.platform.scheduler.web.server.service.inter;

import com.bigdata.platform.scheduler.dal.po.LogMap;
import com.bigdata.platform.scheduler.logger.vo.LogVO;

/**
 * @author xiejiajun
 */
public interface LogReader {

    /**
     * 读取日志
     * @param logMap
     * @return
     */
    LogVO read(LogMap logMap);
}
