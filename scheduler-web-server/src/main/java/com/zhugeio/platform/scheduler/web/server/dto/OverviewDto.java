package com.zhugeio.platform.scheduler.web.server.dto;

import com.zhugeio.platform.scheduler.common.bean.Pager;
import com.zhugeio.platform.scheduler.dal.enums.TaskStatisticDim;
import lombok.Data;

/**
 * @author xiejiajun
 */
@Data
public class OverviewDto extends Pager {

    private String startTime;

    private String endTime;

    private TaskStatisticDim statisticDim;

}
