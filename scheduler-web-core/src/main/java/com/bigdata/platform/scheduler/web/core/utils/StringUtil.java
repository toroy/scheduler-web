package com.bigdata.platform.scheduler.web.core.utils;

import java.util.List;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;

public class StringUtil {

	public static String getRand(List<String> datas) {
		if (CollectionUtils.isEmpty(datas)) {
			return null;
		}
		
		if (datas.size() == 1) {
			return datas.get(0);
		}
		
		int rand = new Random().nextInt(datas.size());
		return datas.get(rand);
	}
}
