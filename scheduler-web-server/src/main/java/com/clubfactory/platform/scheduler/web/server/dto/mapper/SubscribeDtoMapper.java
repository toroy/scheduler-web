package com.clubfactory.platform.scheduler.web.server.dto.mapper;

import com.clubfactory.platform.meta.client.utils.DbUtil;
import com.clubfactory.platform.scheduler.dal.po.AlertSub;

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
