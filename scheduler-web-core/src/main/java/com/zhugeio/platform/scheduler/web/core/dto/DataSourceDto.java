package com.zhugeio.platform.scheduler.web.core.dto;

import com.zhugeio.platform.scheduler.dal.enums.CommonStatus;
import com.zhugeio.platform.scheduler.dal.enums.DbFeatureEnum;
import com.zhugeio.platform.scheduler.dal.enums.DbType;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class DataSourceDto implements Serializable {
    private static final long serialVersionUID = 5230917609344258988L;

    /**
     * 数据源名称
     */
    private String dsName;

    /**
     * 数据源类型
     */
    private DbType dsType;

    /**
     * 数据源登录名
     */
    private String dsUser;

    /**
     * 数据源登录密码
     */
    private String dsPassword;

    /**
     * 数据源登录URL
     */
    private String dsUrl;

    /**
     * 数据源用途
     */
    private DbFeatureEnum feature;

    /**
     * 数据源状态
     */
    private CommonStatus status;
}
