package com.bigdata.platform.scheduler.web.server.vo;

import com.bigdata.platform.scheduler.dal.enums.IEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * @author xiejiajun
 */
@Data
@NoArgsConstructor
public class CommonEnumVo implements Serializable {
    private static final long serialVersionUID = -7120386909557271003L;

    private String key;

    private String value;

    /**
     * @param iEnum
     */
    public CommonEnumVo(IEnum iEnum){
        this.key = iEnum.getDesc();
        this.value = iEnum.name();
    }

    public CommonEnumVo(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
