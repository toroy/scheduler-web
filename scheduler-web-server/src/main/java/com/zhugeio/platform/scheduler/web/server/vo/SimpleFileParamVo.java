package com.zhugeio.platform.scheduler.web.server.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO
 *
 * @author zhoulijiang
 * @date 2025/6/16 08:19
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleFileParamVo {

    private Long fileParamId;

    private String fileParamName;

    private Integer fileParamVersion;
}
