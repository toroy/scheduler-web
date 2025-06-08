package com.zhugeio.platform.scheduler.web.server.dto;

import com.zhugeio.platform.scheduler.common.bean.Pager;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class ClusterPagerDto extends Pager {
    private static final long serialVersionUID = 9179921922786170877L;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 集群URL
     */
    private String url;

    /**
     * 团队名称
     */
    private String departName;
    
}
