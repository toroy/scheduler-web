package com.zhugeio.platform.scheduler.web.server.service.inter;

import com.zhugeio.platform.scheduler.common.bean.PageUtils;
import com.zhugeio.platform.scheduler.dal.po.BaseJob;

public interface JobPageCallback {

	public PageUtils<BaseJob> doInPageList(BaseJob job);
}
