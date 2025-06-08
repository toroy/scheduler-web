package com.zhugeio.platform.scheduler.web.server.vo;

import lombok.Data;

import java.util.Map;

/**
 * @author xiejiajun
 */
@Data
public class SimpleDataSourceVo {

    /**
     * 用于传递参数
     */
    private String dsName;

    /**
     * 用于展示的数据源名称
     */
    private String desc;

    public static SimpleDataSourceVo dto2Vo(String dsName, Map<String,String> dsMap) {
        SimpleDataSourceVo simpleDataSourceVo = new SimpleDataSourceVo();
        simpleDataSourceVo.setDesc(dsMap.getOrDefault(dsName, "废弃数据源"));
        simpleDataSourceVo.setDsName(dsName);
        return simpleDataSourceVo;
    }
}
