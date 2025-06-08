package com.zhugeio.platform.scheduler.web.server.service.inter;

import com.zhugeio.platform.scheduler.dal.po.LogMap;
import com.zhugeio.platform.scheduler.logger.vo.LogVO;

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
