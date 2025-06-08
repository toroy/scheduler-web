package com.zhugeio.platform.scheduler.web.server.constant;

public class NoticeConstant {

	public static String CHECK_BODY = "`新盖亚`任务:[%s](%s)， 申请审核\n"
			+ "**任务详情**\n"
			+ "> 申请人: <font color=\"info\">%s</font>\n"
			+ "> 任务id: <font color=\"comment\">%s</font>\n"
			+ "> 任务创建者: <font color=\"comment\">%s</font>\n"
			+ "> 调度类型: <font color=\"comment\">%s</font>\n"
			+ "> 任务类型: <font color=\"comment\">%s</font>\n"
			+ "> 目标表表名: <font color=\"comment\">%s</font>\n"
			+ "> 上次审核人: <font color=\"comment\">%s</font>\n"
			+ "> 是否临时集群: <font color=\"comment\">%s</font>\n"
			+ "> 调度机: <font color=\"comment\">%s</font>\n"
			+ "> 优先级: <font color=\"comment\">%s</font>\n"
			+ "> 是否测试: <font color=\"comment\">%s</font>\n"
			+ "> 最近测试时间: <font color=\"comment\">%s</font>\n"
			+ "> 测试耗时: <font color=\"comment\">%s</font>\n\n"
			+ "脚本查看: [查看地址](%s)\n"
			+ "审核: [审核通过](%s)\n";
	
	public static String CHECK_SUCCESS_BODY = "`新盖亚`任务:[%s](%s)， 已通过审核， 审核人：%s";

    public static String CHECK_USER_SUCCESS_BODY = "`新盖亚`任务:%s，任务id:%s，已通过审核";
}
