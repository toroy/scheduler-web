package com.bigdata.platform.scheduler.web.server.constant;

public final class JobConstant {

	public static final String HIVE_VAR = "--hivevar ";
	
	// 系统随机机器id
	public static final Long SYSTEM_MACHINE_ID = 0L;
	
	// 自动审核，重试的最大限制
	public static final Integer CHECK_RETRY_MAX = 10;
	
	// 自动审核，测试实例最大耗时时间（单位 秒）
	public static final Integer CHECK_TEMP_TASK_DUR_MAX = 30 * 60;
	
	public static final String SYSTEM_MACHINE_NAME = "系统随机";
	
	/**
	 * json 文件扩展名
	 */
	public final static String JSON_EXT = "json";
			
	/**
	 * py 文件扩展名
	 */
	public final static String PYTHON_EXT = "py";
	
	/**
	 * json注解的正则表达式
	 */
	public final static String JSON_REMARK_REGEX = "/\\*{1,2}[\\s\\S]*?\\*/";
}
