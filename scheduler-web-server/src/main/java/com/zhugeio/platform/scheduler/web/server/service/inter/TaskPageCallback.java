package com.zhugeio.platform.scheduler.web.server.service.inter;

import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.dal.po.Task;

public interface TaskPageCallback {

	public PageUtils<Task> doInPageList(Task task);
}
