package com.zhugeio.platform.scheduler.web.server.dto;

import com.zhugeio.platform.scheduler.common.bean.Pager;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class DataSourcePagerDto extends Pager {

    private String dsName;

    private String createUser;

    private String dsUrl;
}
