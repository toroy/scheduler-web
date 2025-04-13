package com.clubfactory.platform.scheduler.web.server.service.inter;

import com.clubfactory.platform.scheduler.common.bean.PageUtils;
import com.clubfactory.platform.scheduler.dal.po.BaseJob;

public interface JobPageCallback {

	public PageUtils<BaseJob> doInPageList(BaseJob job);
}
