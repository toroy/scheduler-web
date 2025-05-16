package com.bigdata.platform.scheduler.web.core.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiejiajun
 */
@Data
public class ScriptContentVo implements Serializable {

    /**
     * 脚本ID
     */
    private Long id;

    /**
     * 脚本内容
     */
    private String content;
    
    /**
     * 扩展名
     */
    private String fileExt;
}
