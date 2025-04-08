package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.scheduler.dal.enums.DbType;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class DataSourceTestDto implements Serializable {

    private static final long serialVersionUID = -2565224371279676131L;

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
     * 数据源ID：用于判断编辑接口的数据源测试是否需要解密
     */
    private Long dsId;
}
