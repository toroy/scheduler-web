package com.bigdata.platform.scheduler.web.server.service.inter;

import com.bigdata.platform.scheduler.common.bean.PageUtils;
import com.bigdata.platform.scheduler.dal.po.Task;

public interface TaskPageCallback {

	public PageUtils<Task> doInPageList(Task task);
}
