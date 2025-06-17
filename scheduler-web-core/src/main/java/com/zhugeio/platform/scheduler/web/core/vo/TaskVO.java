package com.zhugeio.platform.scheduler.web.core.vo;

import com.zhugeio.platform.scheduler.dal.po.Task;

import lombok.Data;

@Data
public class TaskVO extends Task {

	private String targetName;
}
