package com.bigdata.platform.scheduler.web.server.dqc.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DqcUserVo implements Serializable {

    private static final long serialVersionUID = 6767432486775977171L;

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户名字
     */
    private String name;
}
