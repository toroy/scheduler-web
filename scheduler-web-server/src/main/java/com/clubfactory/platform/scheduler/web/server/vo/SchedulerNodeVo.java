package com.clubfactory.platform.scheduler.web.server.vo;

import com.clubfactory.platform.scheduler.dal.enums.CommonStatus;
import com.clubfactory.platform.scheduler.dal.enums.MachineTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author xiejiajun
 */
@Data
public class SchedulerNodeVo implements Serializable {
    private static final long serialVersionUID = -845055528846394655L;

    /**
     * id
     */
    private Long id;

    /**
     * 创建人:Long -> String
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 调度机名称：name
     */
    private String machineName;

    /**
     * 调度机类型:type
     */
    private MachineTypeEnum machineType;

    /**
     * 调度机IP
     */
    private String ip;

    /**
     * 调度机功能
     */
    private String functions;

    /**
     * 调度机功能描述
     */
    private String functionsDesc;

    /**
     * 调度机启停状态
     */
    private String status;

    /**
     * 调度机状态描述：用于展示
     */
    private String statusDesc;

    /**
     * 调度机槽位数量
     */
    private Integer slots;


}
