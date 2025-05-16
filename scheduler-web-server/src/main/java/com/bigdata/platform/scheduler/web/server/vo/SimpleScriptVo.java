package com.bigdata.platform.scheduler.web.server.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiejiajun
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleScriptVo {
    private Long scriptId;

    private String scriptName;

    private Integer scriptVersion;
}
