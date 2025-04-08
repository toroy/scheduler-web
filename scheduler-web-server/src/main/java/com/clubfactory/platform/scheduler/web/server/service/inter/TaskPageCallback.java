package com.clubfactory.platform.scheduler.web.server.service.inter;

import com.clubfactory.platform.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.dal.po.Task;

public interface TaskPageCallback {

	public PageUtils<Task> doInPageList(Task task);
}
