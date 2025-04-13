package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.scheduler.common.bean.Pager;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class SchedulerNodePagerDto extends Pager {
    private static final long serialVersionUID = 4919412169820284143L;

    /**
     * 调度机名称
     */
    private String machineName;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * ip
     */
    private String ip;
}
