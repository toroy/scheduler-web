package com.bigdata.platform.scheduler.web.server.service.inter;


/**
 * @author xiejiajun
 */
public interface ViewTransformer<T,R> {

    /**
     * vo转换
     * @param po
     * @return
     */
    R transform(T po);
}
