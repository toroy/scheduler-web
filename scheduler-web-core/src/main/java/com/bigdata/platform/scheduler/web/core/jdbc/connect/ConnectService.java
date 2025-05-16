/**
 * File generated at: 2018年10月12日下午4:59:16
 */
package com.bigdata.platform.scheduler.web.core.jdbc.connect;

import com.bigdata.platform.scheduler.web.core.jdbc.dto.DbDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.alibaba.druid.pool.DruidDataSource;
import com.bigdata.platform.scheduler.web.core.enums.CacheEnum;
import com.bigdata.platform.scheduler.web.core.jdbc.constant.Constant;
import com.bigdata.platform.scheduler.web.core.utils.GuavaCacheUtil;

/**
 * 链接维护类
 *
 * @author 周利江
 * @Date 2018年10月12日下午4:59:16
 */
@Service
public class ConnectService {

//    private volatile ConcurrentMap<String, DataSourceConnect> dataSourceConnectMap = new ConcurrentHashMap<String, DataSourceConnect>();
//    private volatile ConcurrentMap<String, DruidDataSource> dataSourceMap = new ConcurrentHashMap<String, DruidDataSource>();
//    private volatile ConcurrentMap<String, DbDto> dbDtoMap = new ConcurrentHashMap<String, DbDto>();

    public void init(DbDto dbDto) {
        init(dbDto, dbDto.getDbUrl());
    }

	public void init(DbDto dbDto, String dbUrl) {
		// Assert.notNull(dbDto.getDbHost(), "db地址");
		// Assert.notNull(dbDto.getDbName(), "db库名");
		// Assert.notNull(dbDto.getDbPort(), "db端口");
		Assert.notNull(dbDto.getDbUser(), "db用户名");
		// Assert.notNull(dbDto.getDbPwd(), "db密码");
		Assert.notNull(dbDto.getDbType(), "db类型");
		
		String conenctKey = CacheEnum.CONNECT.getKey(dbUrl);
		if (GuavaCacheUtil.get(conenctKey) == null) {
			synchronized (this) {
				if (GuavaCacheUtil.get(conenctKey) == null) {
					GuavaCacheUtil.put(CacheEnum.DB_DATA.getKey(dbUrl), dbDto);

					DruidDataSource dataSource = new DruidDataSource();
					dataSource.setUsername(dbDto.getDbUser());
					dataSource.setDriverClassName(dbDto.getDbType().getDriverClassName());
					dataSource.setPassword(dbDto.getDbPwd());
					if (StringUtils.isNotBlank(dbDto.getDbUrl())) {
						dataSource.setUrl(dbDto.getDbUrl());
					} else {
						dataSource.setUrl(makeUrl(dbDto));
					}
					GuavaCacheUtil.put(CacheEnum.DRUID_DATA_SOURCE.getKey(dbUrl), dataSource);

					DataSourceConnect connect = new DataSourceConnect(dataSource);
					GuavaCacheUtil.put(conenctKey, connect);
				}
			}
		}
	}

    public NamedParameterJdbcTemplate getJdbcTemplate(String dbUrl) {
    	String conenctKey = CacheEnum.CONNECT.getKey(dbUrl);
    	DataSourceConnect connect = GuavaCacheUtil.get(conenctKey);
        if (connect == null) {
            return null;
        }
        return connect.getJdbcTemplate();
    }

    public JdbcTemplate getOrgJdbcTemplate(String dbUrl) {
    	String conenctKey = CacheEnum.CONNECT.getKey(dbUrl);
        DataSourceConnect connect = GuavaCacheUtil.get(conenctKey);
        if (connect == null) {
            return null;
        }
        return connect.getOrgJdbcTemplate();
    }

    public DbDto getDb(String dbUrl) {
    	return GuavaCacheUtil.get(CacheEnum.DB_DATA.getKey(dbUrl));
    }
    

    public void close(String dbUrl) {
    	String conenctKey = CacheEnum.CONNECT.getKey(dbUrl);
        DataSourceConnect dataSourceConnect = GuavaCacheUtil.get(conenctKey);
        if (dataSourceConnect != null) {
        	String key = CacheEnum.DRUID_DATA_SOURCE.getKey(dbUrl);
            DruidDataSource dataSource = GuavaCacheUtil.get(key);
            if (dataSource != null) {
                dataSourceConnect.close(dataSource);
                dataSource.close();
            }
        }
        GuavaCacheUtil.clear(conenctKey);
        GuavaCacheUtil.clear(CacheEnum.DRUID_DATA_SOURCE.getKey(dbUrl));
        GuavaCacheUtil.clear(CacheEnum.DB_DATA.getKey(dbUrl));
    }

    private String makeUrl(DbDto dbDto){
        String url = String.format(Constant.JDBC_CONNECT, dbDto.getDbType().getDbName(), dbDto.getDbHost(), dbDto.getDbPort(), dbDto.getDbName());
        if(dbDto.getProp() != null){
            url = url+"?"+dbDto.getProp();
        }
        return url;
    }
}
