package com.bigdata.platform.scheduler.web.client.dto;

import lombok.Data;

import java.io.Serializable;


@Data
public class UserDto implements Serializable {
    private static final long serialVersionUID = 4307562766849203077L;

    private String uid;

    private String name;

    private Integer departId;

    private String departName;
}
