package com.bigdata.platform.scheduler.web.core.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class DepartmentVo implements Serializable {
    private static final long serialVersionUID = 56091842233465935L;

    /**
     * 部门ID
     */
    private Integer departId;

    /**
     * 三级部门名称
     */
    private String departName;
}
