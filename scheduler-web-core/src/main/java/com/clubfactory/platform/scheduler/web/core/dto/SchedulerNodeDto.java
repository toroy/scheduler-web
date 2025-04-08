package com.clubfactory.platform.scheduler.web.core.dto;

import com.clubfactory.platform.scheduler.dal.enums.CommonStatus;
import com.clubfactory.platform.scheduler.dal.enums.MachineTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class SchedulerNodeDto implements Serializable {
    private static final long serialVersionUID = 8703841428040133077L;
    /**
     * 调度机名称
     */
    private String machineName;

    /**
     * 调度机类型
     */
    private MachineTypeEnum machineType;

    /**
     * 调度机IP
     */
    private String ip;

    /**
     * CAL_PYTHON、COLLECT_PYTHON等
     * 调度机类型
     */
    private String functions;

    /**
     * 调度机状态
     */
    private CommonStatus status;

    /**
     * 调度机槽位
     */
    private Integer slots;
}
