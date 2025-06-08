package com.zhugeio.platform.scheduler.web.server.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

public class FastJsonHttpMessageConverterEx extends FastJsonHttpMessageConverter {

	public FastJsonHttpMessageConverterEx() {
	}
	
	protected boolean supports(Class<?> clazz) {
		return super.supports(clazz);
	}
}
