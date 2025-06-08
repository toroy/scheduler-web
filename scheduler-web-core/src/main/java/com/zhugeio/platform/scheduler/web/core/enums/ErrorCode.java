package com.zhugeio.platform.scheduler.web.core.enums;

import com.zhugeio.platform.scheduler.common.constant.IErrorCode;


public enum ErrorCode implements IErrorCode {

	UNKNOWN_EXCEPTION(10000, "未知错误: %s"),
	NOT_ADMIN_ERROR(19000, "没有管理权限"),
	// 实例依赖错误
	NOT_LEAF_NODE_ERROR(20000, "非叶子节点,无法删除"),
	NOT_ALLOW_DEPENDS(20001,"%s【%s】子节点 不能依赖 %s【%s】父节点"),
	NO_CHILD_TASK(20002, "没有子实例"),
	NOT_ALLOW_PREV_DEPEND(20001,"前一周期任务只支持天任务的相互依赖"),
	// sso
	TOKEN_CHECK_ERROR(20005, "单点登录报错"),
	// 新盖亚
	NOW_ALLOW_ADD(20007, "新增功能未开放"),
	NOW_ALLOW_DEL(20007, "删除功能未开放"),
	// DB 源操作
	DB_NOT_EXIST(20010,"db不存在，请检查数据源名称是否正确 %s"),
	DB_CONTANT_JOB_ONLINE_ID(20213, "线上任务引用了该数据源，任务id为:%s"),
	// 实例错误
	TASK_INSTANCE_EXISTS(20100,"任务实例已存在, 存在的实例id:%s"),
	TASK_INSTANCE_NOT_EXISTS(20101,"任务实例不存在"),
	TASK_LOG_NOT_EXISTS(20105,"任务实例不存在"),
	TASK_INSTANCE_EXEC_HOST_IS_NULL(20102,"任务实例执行机IP为空"),
	TASK_LOG_HOST_IS_NULL(20106,"任务实例执行机IP为空"),
	TASK_INSTANCE_LOG_PATH_IS_NULL(20103,"暂无当前任务相关的日志信息"),
	TASK_LOG_PATH_IS_NULL(20107,"暂无当前任务相关的日志信息"),
	TASK_NOT_PERMISSION_ERROR(20104,"无权限操作该实例"),
	TASK_NOT_ALLOW_RERUN(20105,"实例: %s 正在运行，请停止或强制成功后，再重跑"),
	TASK_NOT_PERMISSION(20106, "实例无权操作"),
	// 实时接入
	MQ_NOT_EXISTS(20110,"该实时数据不存在，或无权查看"),
	CREATE_TOPIC_ERROR(20111, "创建topic失败， 失败原因：%s"),
	EXECUTE_HIVE_SQL_ERROR(20112, "执行sql表失败，失败原因： %s"),
	MQ_NOT_PERMISSION(20113, "无权操作"),
	GEN_JOB_REPEAT_ERROR(20114, "任务已经生成，任务id:%s"),
	MQ_HIVE_DB_NOT_SET(20115,"采集的hive库没有配置"),
	MQ_DB_NOT_SUPPORT(20116, "数据采集目前只支持 %s库"),
	// 脚本错误
	SCRIPT_NOT_EXISTE(20130,"脚本不存在, 名称：%s"),
	SCRIPT_NOT_PERMISSION(20131,"脚本没有权限, 脚本名称：%s"),
	SCRIPT_LOCAL_FILE_DELETE_EXCEPTION(20140, "脚本本地文件删除异常, 文件名: %s"),
	SCRIPT_LOCAL_FILE_DELETE_ERROR(20141, "脚本本地文件删除失败, 内容: %s"),
	SCRIPT_LOCAL_FILE_EXEC_ERROR(20142, "脚本本地文件执行异常, 内容: %s"),
	// 调度机错误
	MACHINE_NOT_EXISTE(20160,"调度机不存在, ip：%s"),
	// 任务错误
	JOB_NOT_PERMISSION(20200, "无权操作任务, 任务名：%s"),
	JOB_NOT_EXISTE(20201,"任务不存在"),
	JOB_CHECK_BY_MYSELF_ERROR(20202,"不允许自己审核自己的任务"),
	JOB_PAUSE_ERROR(20203, "线上没有任务可以停止调度，请检查有没有未审核的任务"),
	JOB_COLLECT_TARGET_TABLE_REPEAT(20204,"采集目标表重复,目标库id:%s,  目标表名: %s，重复的任务id: %s"),
	JOB_CHECK_NOW_ALLOW(20205, "审核不能包含已上线或审核失败的任务，任务id：%s"),
	JOB_NAME_REPEAT_ERROR(20206, "任务名重复，名称: %s"),
	JOB_NOT_CHECK(20207, "任务已审核，或已撤销审核状态"),
	JOB_ONLINE_NAME_REPEAT_ERROR(20208, "任务名与线上其他任务名称重复，名称: %s"),
	JOB_DEPEND_CONFIG_ERROR(20210, "依赖配置不合法"),
	JOB_NOT_ALLOW_DELETE_ERROR(20211, "不允许删除"),
	JOB_CONTANT_MACHINE_ID(20212, "线下任务引用了该调度机，任务名为:%s"),
	JOB_ONLINE_CONTANT_MACHINE_ID(20213, "线上任务引用了该调度机，任务名为:%s"),
	JOB_NOT_ONLINE_ERROR(20214,"任务未上线"),
	// 任务依赖
	JOB_CYCLE_ERROR(20300,"循环依赖报错"),
	JOB_DEPEND_TYPE_NOT_EMPTY(20301,"依赖类型没有配置"),
	JOB_ADD_TASK_AFTER_NOW(20302, "实例补录不能晚于当前时间补录"),
	JOB_NOT_ALLOW_PAUSE(20303, "非线上任务，不能暂停，任务id: %s"),
	JOB_NOT_ALLOW_RESUME(20304, "非暂停任务，不能恢复，任务id: %s"),
	JOB_COLLECT_NOT_COPY(20305, "采集任务不允许复制，任务id: %s"),
	// 任务组
	GROUP_NOT_USER_SELF(20310, "此组非用户本人创建, 不能上传任务到该组"),
	GROUP_JOBNAME_NOT_USER_SELF(20311, "此组的任务名与别人任务名冲突，请重命名"),
	// 报警内容
	ALARM_CONTENT_INVALID(20330, "报警内容格式输入有误，请检查"),
	ALARM_NOT_EXIST_USER(20331, "报警组缺少联系人"),
	ALARM_NOT_EXIST_PHONE(20332, "电话报警组缺少手机号"),
	ALARM_NOT_EXIST_EMAIL(20333, "邮件报警组缺少邮箱"),
	ALARM_NOT_EXIST_IM(20334, "IM报警组缺少机器人"),
	// 集群报错
	CLUSTER_NOT_EXIST_ERROR(20400,"任务:%s 没有可用集群"),
	KAFKA_NOT_ALLOW_ERROR(20500, "非kafka源配置，不能修改"),
	// 数据库相关错误
	NON_ADMIN_ERROR(21000,"【%s】只允许管理员操作,用户[%s]还不是管理员"),
	NON_ADMIN_OR_OWNER_ERROR(21001,"【%s】只允许管理员或创建人操作,用户[%s]无权操作"),
	DATASOURCE_CONN_ERROR(21100, "连接失败: %s"),
	HANDLE_PROJECT_ERROR(22000,"%s 项目失败"),
	// 项目错误
	PROJECT_NOT_PERMISSION(22100, "项目无权操作"),
	// DQC错误
	DQC_RULE_MAX_NUM_ERROR(22150, "规则数已达上限，单表规则数，最多 %s 条"),
	DQC_DELETE_PARTITION_RULE_EXIST_ERROR(22151, "请先删除对应的规则，规则名字 %s"),
	DQC_TABLE_NOT_OWNER(22152, "请先认领表"),
	DQC_TABLE_NOT_PERMISSION(22152, "无权操作，请联系表owner"),
	// dubbo报错
	API_META_ERROR(22200, "调用meta失败"),
	NO_PERMISSION(22300, "无权限操作");
	;

	private Integer errorCode;
    private String errorMsg;
    private String[] params;
    
    ErrorCode(Integer errorCode, String errorMsg, String... params) {
    	this.errorCode = errorCode;
    	this.errorMsg = errorMsg;
    	this.params = params;
    }
    
    @Override
    public String getErrorMsg() {
    	if (params != null && params.length != 0 && errorMsg.contains("%s")) {
    		try {
				return String.format(errorMsg, params).replace("%s", "");
			} catch (Exception e) {
			}
    	}
		return errorMsg;
    }
    
    public ErrorCode setParams(String... params) {
    	this.params = params;
    	return this;
    }

	@Override
	public Integer getErrorCode() {
		return this.errorCode;
	}

}
