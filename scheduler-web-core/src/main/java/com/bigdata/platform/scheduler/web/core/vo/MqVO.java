package com.bigdata.platform.scheduler.web.core.vo;

import com.bigdata.platform.scheduler.dal.po.Mq;

import lombok.Data;

@Data
public class MqVO extends Mq {

	private String dsName;
	
	/**
	 * kafak安全配置
	 */
	private String kafkaSaslMechanism;
	
	/**
	 * kafak安全配置
	 */
	private String kafkaSecurityProtocol;
	
	/**
	 * 几分钟拉取一次数据并写临时目录
	 */
	private Integer kafkaSyncWriteFsPeriodMinutes;
	
	/**
	 * 几分钟写一次hive表
	 */
	private Integer kafkaSyncWriteHivePeriodMinutes;
	
	/**
	 * 几分钟一次合并今天分区小文件
	 */
	private Integer kafkaSyncMergeHivePartiPeriodMinutes;
	
	/**
	 * 类似watermarker概念,数据最大迟到的分钟
	 */
	private Integer kafkaSyncWaterMarkerCurMinuteDecrX;
	
}
