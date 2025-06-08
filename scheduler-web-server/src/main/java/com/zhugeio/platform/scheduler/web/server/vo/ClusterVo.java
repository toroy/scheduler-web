package com.zhugeio.platform.scheduler.web.server.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xiejiajun
 */
@Data
public class ClusterVo implements Serializable {
    private static final long serialVersionUID = 4527649595020480202L;

    /**
     * 集群ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String proxyUser;

    /**
     * 代理用户密码
     */
    private String proxyPassword;

    /**
     * 创建人:Long -> String
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 集群URL
     */
    private String url;

    /**
     * 集群功能
     */
    private String functions;

    /**
     * 集群状态
     */
    private String status;

    /**
     * 集群状态描述：用于展示
     */
    private String statusDesc;

    /**
     * 集群所属团队: Long -> String
     */
    private String departName;

    /**
     * 团队ID
     */
    private Integer departId;
    
    /**
     * 集群类型
     */
    private String typeDesc;

    /**
     * 集群类型
     */
    private String type;

    /**
     * 集群对应的yarn RM hosts
     */
    private String yarnRMHosts;
    /**
     * 集群对应的yarn RM http port
     */
    private String yarnRMHttpPort;
    /**
     * 集群对应的yarn超级用户
     */
    private String yarnSuperUser;
}
