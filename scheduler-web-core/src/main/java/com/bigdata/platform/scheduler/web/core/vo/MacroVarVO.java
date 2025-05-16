package com.bigdata.platform.scheduler.web.core.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
@Builder
public class MacroVarVO {

    private String varName;

    private String varExpr;
}
