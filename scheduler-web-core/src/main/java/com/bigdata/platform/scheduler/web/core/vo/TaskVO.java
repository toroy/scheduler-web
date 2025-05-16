package com.bigdata.platform.scheduler.web.core.vo;

import com.bigdata.platform.scheduler.dal.po.Task;

import lombok.Data;

@Data
public class TaskVO extends Task {

	private String targetName;
}
