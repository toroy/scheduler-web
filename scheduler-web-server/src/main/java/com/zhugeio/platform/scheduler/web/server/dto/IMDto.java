package com.zhugeio.platform.scheduler.web.server.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class IMDto implements Serializable {

	private static final long serialVersionUID = -609849669886685983L;

	/**
	 * 指定接收消息的成员, 当touser为”@all”时忽略本参数
	 */
	private String touser;
	
	/**
	 * 消息类型
	 */
	private String msgtype;
	
	/**
	 * 正文内容
	 */
	private Text text;
	
	/**
	 * markdown格式
	 */
	private Markdown markdown;
	
	@Data
	public static class Text {
		
		/**
		 * 消息内容
		 */
		private String content;

        /**
         * 需要@的用户，使用uid，全部 @all
         */
		@JSONField(name = "mentioned_list")
		private List<String> mentionedList;


        /**
         * 需要@的用户，使用手机号，全部 @all
         */
        @JSONField(name = "mentioned_mobile_list")
        private List<String> mentionedMobileList;
	}
	
	@Data
	public static class Markdown {
		
		/**
		 * 消息内容
		 */
		private String content;
		
	}
}
