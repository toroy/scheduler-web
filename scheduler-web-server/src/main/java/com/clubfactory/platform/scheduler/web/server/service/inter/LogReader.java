package com.clubfactory.platform.scheduler.web.server.service.inter;

import com.clubfactory.platform.scheduler.dal.po.LogMap;
import com.clubfactory.platform.scheduler.logger.vo.LogVO;
import com.clubfactory.platform.scheduler.web.core.vo.TaskVO;

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
