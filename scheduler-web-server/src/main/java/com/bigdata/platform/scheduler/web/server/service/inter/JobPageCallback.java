package com.bigdata.platform.scheduler.web.server.service.inter;

import com.bigdata.platform.scheduler.common.bean.PageUtils;
import com.bigdata.platform.scheduler.dal.po.BaseJob;

public interface JobPageCallback {

	public PageUtils<BaseJob> doInPageList(BaseJob job);
}
