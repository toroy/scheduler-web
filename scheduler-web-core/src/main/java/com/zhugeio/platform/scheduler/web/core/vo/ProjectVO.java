package com.zhugeio.platform.scheduler.web.core.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ProjectVO {
    private Long projectId;
    private String projectName;
    private String description;
    private Date createTime;
    private Date updateTime;
    private Long createUser;
    private String createUserStr;
    private Long updateUser;
    private String updateUserStr;

}
