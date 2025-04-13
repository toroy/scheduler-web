package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.scheduler.common.bean.Pager;

import lombok.Data;

@Data
public class MqQueryDto extends Pager {

	private static final long serialVersionUID = 7439787253722799790L;

	/**
	 * topic名称
	 */
	private String topicName;
	
	/**
	 * topic描述
	 */
	private String topicDesc;
	
	/**
	 * 创建人
	 */
	private String userName;
}
