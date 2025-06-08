package com.zhugeio.platform.scheduler.web.client.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class SubscribeUserDto implements Serializable {

    private static final long serialVersionUID = -7453098411706210822L;

    /**
     * 订阅人姓名
     */
    private String username;

    /**
     * 手机号
     */
    private String phoneNo;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 机器人URL
     */
    private String imRobot;
}
