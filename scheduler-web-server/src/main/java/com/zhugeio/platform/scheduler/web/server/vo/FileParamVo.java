package com.zhugeio.platform.scheduler.web.server.vo;

import com.zhugeio.platform.scheduler.dal.po.FileParam;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * TODO
 *
 * @author zhoulijiang
 * @date 2025/6/16 08:16
 **/
@Data
public class FileParamVo implements Serializable {

    private static final long serialVersionUID = 5857708771061850982L;
    /**
     * 脚本ID
     */
    private Long id;

    /**
     * 脚本名称
     */
    private String fileParamName;

    /**
     * 文件下载用的filename: scriptName + "." + fileExt
     */
    private String downloadFileName;

    /**
     * 脚本描述
     */
    private String desc;

    /**
     * 团队名称
     */
    private String departName;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 脚本类型
     */
    private String fileParamType;

    /**
     * 脚本类型描述
     */
    private String fileParamTypeDesc;

    /**
     * 是否可编辑/在线浏览
     */
    private Boolean modifiable;

    /**
     * 脚本最新版本号
     */
    private Integer version;

    /**
     * 1：开启同步， 0：关闭同步
     */
    private Integer isSync;

    public Integer getIsSync() {
        return 0;
    }
}
