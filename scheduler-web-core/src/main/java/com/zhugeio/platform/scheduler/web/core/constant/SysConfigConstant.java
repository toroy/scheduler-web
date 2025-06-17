package com.zhugeio.platform.scheduler.web.core.constant;

public final class SysConfigConstant {

	// 超时限制，单位秒
	public final static String JOB_COLLECT_TIME_OUT = "job.collect.timeout";
	
	// 超时限制，单位秒 -1不限制
	public final static String JOB_REFLUE_TIME_OUT = "job.reflue.timeout";
	
	// 超时限制，单位秒 -1不限制
	public final static String JOB_CAL_TIME_OUT = "job.cal.timeout";
	
	// mq采集hive默认dbid
	public final static String MQ_HIVE_DB_ID = "mq.hive.db.id";
	
	// mq采集hive默认脚本id
	public final static String MQ_SCRIPT_ID = "mq.script.id";
	
	// topic添加url地址
	public final static String MQ_ADD_TOPIC_URL = "mq.add.topic.url";
	
	// 采集重试间隔时长
	public final static String MQ_JOB_RETRY_DUR = "mq.job.retry.dur";
	
	// 采集重试最大次数
	public final static String MQ_JOB_RETRY_COUNT = "mq.job.retry.count";
	
	// s3存储路径
	public final static String S3_DATA_PATH = "s3.data.path";
	
	// mq任务默认主类
	public final static String MQ_JOB_MAIN_CLASS = "mq.job.main.class";
	
	// 安全协议
	public final static String kafkaSaslMechanism = "kafka.sasl.mechanism";
	
	// 安全协议
	public final static String kafkaSecurityProtocol = "kafka.security.protocol";
	
	// 几分钟拉取一次数据并写临时目录
//	public final static String kafkaSyncWriteFsPeriodMinutes = "kafka.sync.write.fs.period.minutes";
	
	// 几分钟写一次hive表
//	public final static String kafkaSyncWriteHivePeriodMinutes = "kafka.sync.write.hive.period.minutes";
	
	// 几分钟一次合并今天分区小文件
//	public final static String kafkaSyncMergeHivePartiPeriodMinutes = "kafka.sync.merge.hive.parti.period.minutes";
	
	// 类似watermarker概念,数据最大迟到的分钟
//	public final static String kafkaSyncWaterMarkerCurMinuteDecrX = "kafka.sync.water.marker.cur.minute.decr.x";

	//批量生成脚本的模板
	public final static String ASSITANT_DOWNLOAD_PATH_BATCH_SCRIPT = "assitant.download.path.batch.script";

	//生成任务的可执行的python脚本模版
	public final static String ASSITANT_DOWNLOAD_PATH_TASK_PYTHON = "assitant.download.path.task.python";

	//生成任务的json数据脚本模版
	public final static String ASSITANT_DOWNLOAD_PATH_TASK_JSON = "assitant.download.path.task.json";

	//生成任务依赖的json数据脚本模版
	public final static String ASSITANT_DOWNLOAD_PATH_DEP_JSON = "assitant.download.path.dep.json";
	
	// 服务器地址
	public final static String WEB_HOST = "web.host";
	
	// 管理员机器人地址
	public final static String ADMIN_IM_URL = "admin.im.url";

    // 用户群机器人地址
    public final static String USER_IM_URL = "user.im.url";

	// dqc数值型脚本Id
    public final static String DQC_NUMBER_SCRIPT_ID = "dqc.number.script.id";

    // dqc波动型脚本Id
	public final static String DQC_ROLLING_SCRIPT_ID = "dqc.rolling.script.id";
}
