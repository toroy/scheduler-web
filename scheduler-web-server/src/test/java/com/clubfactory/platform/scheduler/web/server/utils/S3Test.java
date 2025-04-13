package com.clubfactory.platform.scheduler.web.server.utils;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.clubfactory.platform.scheduler.common.exception.BizException;
import com.clubfactory.platform.scheduler.web.core.utils.DFSUtils;
import com.clubfactory.platform.scheduler.web.server.BaseTest;

import lombok.extern.log4j.Log4j;

@Log4j
public class S3Test extends BaseTest {

	@Test
	public void getS3ContentTest() {
		String dfsFileName = "/tmp/tableau_log/gip_1.txt";
		String content;
		try {
			if (DFSUtils.getInstance().exists(dfsFileName)) {
				List<String> contentList = DFSUtils.getInstance().catFile(dfsFileName, 0, Integer.MAX_VALUE);
				content = StringUtils.join(contentList, "\n");
			} else {
				content = "DFS上脚本文件不存在";
			}
		} catch (IOException e) {
			throw new BizException(e.getMessage());
		}
		System.out.println("--------------------------------- 开始 ---------------------------- ");
		System.out.println(content);
		log.info("开始");
		log.info(content);
	}
}
