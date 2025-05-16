package com.bigdata.platform.scheduler.web.server.service.inter;

public interface JobEditCallback {

	public void doInEditDetail(Long localUserId, Boolean isAdmin);
}
