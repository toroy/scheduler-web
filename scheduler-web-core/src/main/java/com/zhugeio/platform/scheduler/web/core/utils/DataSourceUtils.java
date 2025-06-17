package com.zhugeio.platform.scheduler.web.core.utils;

import com.zhugeio.platform.scheduler.web.core.dto.DataSourceDto;
import com.zhugeio.platform.scheduler.web.core.enums.ErrorCode;
import com.zhugeio.platform.scheduler.common.exception.BizException;
import com.zhugeio.platform.scheduler.common.util.Assert;
import com.zhugeio.platform.scheduler.dal.enums.DbType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author xiejiajun
 */
@Slf4j
public class DataSourceUtils {

    /**
     * 测试数据源是否能正常连接
     * @param dataSourceDto
     * @return
     */
    public static boolean testDataSource(DataSourceDto dataSourceDto, int timeoutSeconds ){
        boolean isConnectSuccess = false;
        Connection conn = null;
        Assert.notNull(dataSourceDto);
        Assert.notNull(dataSourceDto.getDsType());
        Assert.notNull(dataSourceDto.getDsUrl());
        
        String driverClass = dataSourceDto.getDsType().getDriverClassName();
        try {
            String url = dataSourceDto.getDsUrl();
            String username = dataSourceDto.getDsUser();
            String password = dataSourceDto.getDsPassword();
            // 新版本mysql 驱动自动加载，无需再次加载
            if (dataSourceDto.getDsType() != DbType.MYSQL) {
                Class.forName(driverClass);
            }
            DriverManager.setLoginTimeout(timeoutSeconds);
            Properties props = new Properties();
            props.put("user",username);
            props.put("loginTimeout",String.valueOf(timeoutSeconds));
            if (StringUtils.isNotBlank(password)){
                props.put("password",password);
            }
            conn = DriverManager.getConnection(url,props);
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage());
            throw new BizException(ErrorCode.DATASOURCE_CONN_ERROR.setParams(e.getMessage()));
        }finally {
            if (conn != null){
                isConnectSuccess = true;
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return isConnectSuccess;
    }

}
