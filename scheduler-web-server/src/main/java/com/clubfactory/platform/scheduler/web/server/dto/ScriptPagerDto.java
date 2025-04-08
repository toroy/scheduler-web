package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.common.bean.Pager;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class ScriptPagerDto extends Pager {

    /**
     * 脚本ID
     */
    private Long scriptId;
    
    /**
     * 用于模糊查询的脚本名称
     */
    private String scriptName;

    /**
     * 用于模糊查询的用户名
     */
    private String createUser;

    /**
     * 用于模糊查询的部门名称
     */
    private String departName;

}
