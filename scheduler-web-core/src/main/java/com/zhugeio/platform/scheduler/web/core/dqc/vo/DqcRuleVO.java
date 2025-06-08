package com.zhugeio.platform.scheduler.web.core.dqc.vo;

import com.zhugeio.platform.scheduler.dal.enums.IEnum;
import com.zhugeio.platform.scheduler.dal.po.DqcRule;
import lombok.Data;

@Data
public class DqcRuleVO extends DqcRule {


    // 分区表达式
    private String expression;

    /**
     * 采样方式描述
     */
    private String sampleDesc;

    public String getSampleDesc() {
        return getDesc(this.getSample());
    }

    /**
     * 比较方式描述
     */
    private String compareDesc;

    public String getCompareDesc() {
        return getDesc(this.getCompare());
    }

    /**
     * 校验类型描述
     */
    private String checkTypeDesc;

    public String getCheckTypeDesc() {
        return getDesc(this.getCheckType());
    }

    /**
     * 校验类型描述
     */
    private String checkModeDesc;

    public String getCheckModeDesc() {
        return getDesc(this.getCheckMode());
    }

    /**
     * 字段描述
     */
    private String fieldDesc;

    public String getFieldDesc() {
        return this.getField();
    }



    public String getDesc(IEnum iEnum) {
        if (iEnum == null) {
            return null;
        }
        return iEnum.getDesc();
    }

    /**
     * 创建人
     */
    public String userName;

}
