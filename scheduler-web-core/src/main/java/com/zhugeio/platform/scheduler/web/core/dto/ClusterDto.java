package com.zhugeio.platform.scheduler.web.core.dto;

import com.zhugeio.platform.scheduler.dal.enums.ClusterTypeEnum;
import com.zhugeio.platform.scheduler.dal.enums.CommonStatus;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class ClusterDto implements Serializable {
    private static final long serialVersionUID = -5394604890707486107L;


    /**
     * 集群URL
     */
    private String url;
    /**
     * 用于连接集群的代执行用户
     */
    private String proxyUser;
    /**
     * 代理用户密码
     */
    private String proxyPassword;
    /**
     * 集群功能
     */
    private  String functions;
    /**
     * 集群状态
     */
    private CommonStatus status;
    /**
     * 部门ID
     */
    private Integer departId;
    /**
     * 集群名称
     */
    private String clusterName;
    
    /**
     * 集群类型
     */
    private ClusterTypeEnum type;

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
