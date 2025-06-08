package com.zhugeio.platform.scheduler.web.server.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xiejiajun
 */
@Data
public class DataSourceVo implements Serializable {

    private static final long serialVersionUID = 3787867195905124344L;
    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     */
    private String dsName;

    /**
     * 类型
     */
    private String dsType;

    /**
     * 用户名
     */
    private String dsUser;

    /**
     * 密码
     */
    private String dsPassword;
    
    /**
     * 库名
     */
    private String dbName;

    /**
     * 库url
     */
    private String dsUrl;

    /**
     * 功能：用于前端查询条件
     */
    private String feature;

    /**
     * 功能描述：用于前端展示
     */
    private String featureDesc;

    /**
     * 状态枚举名称
     */
    private String status;

    /**
     * 枚举描述
     */
    private String statusDesc;
    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateUser;

}
