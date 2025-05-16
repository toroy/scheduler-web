package com.bigdata.platform.scheduler.web.server.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class MqQueryVo implements Serializable {

	private static final long serialVersionUID = -7946601801471770972L;

	/**
	 * 主键id
	 */
	private Long id;
	
	/**
	 * topic名字
	 */
	private String topicName;
	
	/**
	 * topic描述
	 */
	private String topicDesc;
	
	/**
	 * topic分区
	 */
	private Integer topicPartition;
	
	/**
	 * 是否生成
	 */
	private Boolean isGenJob;
	
	/**
	 * 创建人
	 */
	private String userName;
	
	/**
	 * 集群名称
	 */
	private String dsName;
	
	/**
	 * 更新时间
	 */
	private Date updateTime;
	
	/**
	 * 任务id
	 */
	private Long jobId;
	
}
