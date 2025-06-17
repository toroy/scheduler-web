package com.zhugeio.platform.scheduler.web.server.service.inter;

public interface JobSaveCallback {

	public void doInSaveDetail(Long localUserId, Long id);
}
