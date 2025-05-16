package com.bigdata.platform.scheduler.web.server.dto;

import com.bigdata.platform.scheduler.common.bean.Pager;
import lombok.Data;


/**
 * @author chen qian
 */
@Data
public class ProjectPagerDto extends Pager {

    private String name;

}
