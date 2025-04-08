package com.clubfactory.platform.scheduler.web.server.vo;

import java.io.Serializable;
import java.util.List;

import org.apache.curator.shaded.com.google.common.collect.Lists;

import lombok.Data;

@Data
public class JobEnumVo implements Serializable {

	private static final long serialVersionUID = -111752865497694386L;

	/**
	 * 任务类型
	 */
	private List<Content> jobTypes;
	
	/**
	 * 任务状态
	 */
	private List<Content> jobStatus;
	
	/**
	 * 目标库
	 */
	private List<Content> dbs;
	
	/**
	 * db源
	 */
	private List<Content> dbSources;
	
	/**
	 * 存储格式
	 */
	private List<Content> formats;
	
	/**
	 * 周期类型
	 */
	private List<Content> cycleTypes;
	
	/**
	 * 采集并发
	 */
	private List<Content> runCounts;
	
	/**
	 * 优先级
	 */
	private List<Content> priority;
	
	/**
	 * 调度机
	 */
	private List<Content> machines;
	
	/**
	 * 告警类型
	 */
	private List<Content> alarms;
	
	/**
	 * 告警方式
	 */
	private List<Content> alarmTypes;
	
	/**
	 * 作业状态
	 */
	private List<Content> taskStatus;
	
	/**
	 * 增长方式
	 */
	private List<Content> incrementTypes;
	
	/**
	 * 任务大类包含小类
	 */
	private List<Content> categroyTypes;
	
	/**
	 * 语言类型
	 */
	private List<Content> programTypes;
	
	/**
	 * 模式类型
	 */
	private List<Content> deployModes;
	
	@Data
	public static class Content {
		
		/**
		 * 描述
		 */
		private String desc;
		
		/**
		 * 值
		 */
		private Object value;
		
		/**
		 * 真实信息
		 */
		private String name;

		/**
		 * 用于标记是否是流任务
		 */
		private boolean isStream;
		
		
		/**
		 * 子
		 */
		private List<Content> childs = Lists.newLinkedList();
		
		private List<Content> cycleTypes = Lists.newLinkedList();
		
		public void addChilds(Content content) {
			this.childs.add(content);
		}
		
		public void addCycleTypes(Content content) {
			this.childs.add(content);
		}
	}
	
	
	
}
