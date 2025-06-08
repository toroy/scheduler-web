package com.zhugeio.platform.scheduler.web.server.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
public class ProjectDto {

    private Long projectId;
    @NotBlank(message = "project name 不能为空")
    private String projectName;
    private String description;
    private Date createTime;
    private Date updateTime;
    private Long createUser;
    private Long updateUser;
}
