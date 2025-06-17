package com.zhugeio.platform.scheduler.web.server.vo;

import com.zhugeio.platform.scheduler.dal.po.JobType;
import com.zhugeio.platform.scheduler.web.core.Constants;
import com.zhugeio.platform.scheduler.web.core.utils.JobTypeCache;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author xiejiajun
 */
@Data
public class TaskOverviewVo implements Serializable {
    private static final long serialVersionUID = 4870926499929677206L;
    private Long id;
    private String taskDate;
    private String type;
    private String departName;
    private Long departId;
    private Integer totalTask;
    private Integer succeedTaskCount;
    private Integer delayTaskCount;
    private Integer failedTaskCount;

    /**
     * 等待运行的任务数（init + ready + scheduled)
     */
    private Integer initTaskCount;
    private String succeedRate;

    /**
     * 根据类型枚举name获取对应的desc
     * @return
     */
    public String getType() {
        if (StringUtils.isBlank(type)){
            return type;
        }
        if (!type.contains(Constants.UNDER_LINE)){
            return type;
        }
        JobType jobType = JobTypeCache.getJobTypeByFunction(type);
        return jobType == null ? type : jobType.getType();
    }


    /**
     * 计算未运行的任务数
     * @return
     */
    public Integer getInitTaskCount() {
        if (totalTask == null){
            return 0;
        }
        int successCnt = Optional.ofNullable(succeedTaskCount).orElse(0);
        int failedCnt = Optional.ofNullable(failedTaskCount).orElse(0);
        return  totalTask - successCnt - failedCnt;

    }
}
