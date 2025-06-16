package com.zhugeio.platform.scheduler.web.server.dto;

import com.zhugeio.platform.scheduler.common.bean.Pager;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class FileParamPagerDto extends Pager {

    /**
     * 脚本ID
     */
    private Long fileParamId;
    
    /**
     * 用于模糊查询的脚本名称
     */
    private String fileParamName;

    /**
     * 用于模糊查询的用户名
     */
    private String createUser;

    /**
     * 用于模糊查询的部门名称
     */
    private String departName;

}
