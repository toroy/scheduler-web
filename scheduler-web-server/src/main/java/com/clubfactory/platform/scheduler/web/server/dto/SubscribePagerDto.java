package com.clubfactory.platform.scheduler.web.server.dto;

import com.clubfactory.platform.common.bean.Pager;
import com.clubfactory.platform.scheduler.dal.po.AlertSub;
import com.clubfactory.platform.scheduler.web.server.login.LocalUser;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author xiejiajun
 */
@Data
public class SubscribePagerDto extends Pager {

    private String dataSource;
    private String dbName;
    private String tableName;

    public AlertSub buildQueryPo() {
        AlertSub alertSub = new AlertSub();
        alertSub.setIsDeleted(false);
        alertSub.setPageNo(this.getPageNo());
        alertSub.setPageSize(this.getPageSize());
        if (StringUtils.isNotBlank(dataSource)) {
            alertSub.setDataSource(dataSource);
        }
        if (StringUtils.isNotBlank(dbName)) {
            alertSub.setDbName(dbName);
        }
        if (StringUtils.isNotBlank(tableName)) {
            alertSub.setTableName(tableName);
        }
        // 只查询当前用户创建(订阅)的条目
        alertSub.setCreateUser(LocalUser.getLoginUserId());
        alertSub.setOrderBy("update_time desc");
        return alertSub;
    }
}
