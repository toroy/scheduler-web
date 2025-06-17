package com.zhugeio.platform.scheduler.web.server.dto;

import lombok.Data;

import java.util.List;

/**
 * @author xiejiajun
 */
@Data
public class UnSubscribeDto {

    /**
     * 要取消的订阅信息ID列表
     */
    private List<Long> idList;
}
