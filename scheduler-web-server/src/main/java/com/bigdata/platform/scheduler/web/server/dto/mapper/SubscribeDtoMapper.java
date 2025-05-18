package com.bigdata.platform.scheduler.web.server.dto.mapper;

import com.bigdata.platform.meta.client.utils.DbUtil;
import com.bigdata.platform.scheduler.dal.po.AlertSub;

/**
 * @author xiejiajun
 */
public class SubscribeDtoMapper {

    /**
     * 从AlertSub对象映射到tableIdentifierKey
     * @param alertSub
     * @return
     */
    public static String mapToTableSimpleDto(AlertSub alertSub) {
        if (alertSub == null) {
            return null;
        }
        return DbUtil.getTableKey(alertSub.getDataSource(), alertSub.getDbName(), alertSub.getTableName());
    }
}
