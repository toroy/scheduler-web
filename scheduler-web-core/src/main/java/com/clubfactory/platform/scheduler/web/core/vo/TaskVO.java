package com.clubfactory.platform.scheduler.web.core.vo;

import com.clubfactory.platform.scheduler.dal.po.Task;

import lombok.Data;

@Data
public class TaskVO extends Task {

	private String targetName;
}
