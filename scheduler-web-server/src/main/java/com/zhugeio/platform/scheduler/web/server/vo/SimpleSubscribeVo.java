package com.zhugeio.platform.scheduler.web.server.vo;

import com.zhugeio.platform.meta.client.dto.TableDto;
import lombok.Data;

import java.util.Map;

/**
 * @author xiejiajun
 */
@Data
public class SimpleSubscribeVo {
    private String dataSource;
    private String dbName;
    private String tableName;
    /**
     * 用于展示的数据源描述（数据源名称)
     */
    private String desc;
    /**
     * 责任人UID
     */
    private String picId;

    public static SimpleSubscribeVo dto2Vo(TableDto dto, Map<String,String> dataSourceMap) {
        SimpleSubscribeVo subscribeVo = new SimpleSubscribeVo();
        subscribeVo.setDataSource(dto.getDbSource());
        subscribeVo.setDbName(dto.getDbName());
        subscribeVo.setTableName(dto.getName());
        subscribeVo.setPicId(dto.getUid());
        subscribeVo.setDesc(dataSourceMap.getOrDefault(dto.getDbSource(), "已废弃数据源"));
        return subscribeVo;
    }
}
