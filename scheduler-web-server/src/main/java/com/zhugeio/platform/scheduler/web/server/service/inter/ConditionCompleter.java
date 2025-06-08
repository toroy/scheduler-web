package com.zhugeio.platform.scheduler.web.server.service.inter;


import com.zhugeio.platform.scheduler.common.bean.PageUtils;

/**
 * @author xiejiajun
 */
public interface ConditionCompleter<T,R> {

    /**
     * 完善查询条件
     * @param queryObj
     * @return
     */
     PageUtils<R> complete(T queryObj);

}
