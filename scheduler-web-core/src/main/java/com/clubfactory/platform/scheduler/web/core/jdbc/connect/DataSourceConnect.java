/**
 * File generated at: 2018年10月12日下午4:47:57
 */
package com.clubfactory.platform.scheduler.web.core.jdbc.connect;

import com.alibaba.druid.pool.DruidDataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 数据源链接，懒汉模式 
 * 
 * @author 周利江
 * @Date 2018年10月12日下午4:47:57
 *
 */
@Slf4j
public class DataSourceConnect {
	
	private volatile NamedParameterJdbcTemplate jdbcTemplate = null;

	private volatile JdbcTemplate orgJdbcTemplate = null;
	
	private DruidDataSource dataSource;
	
	private String url;
	
	private String userName;
	
	private String password;
	
	private final int RETRY_CONNECT = 1;
	
	public DataSourceConnect(String url, String userName, String password) {
		this.url = url;
		this.userName = userName;
		this.password = password;
	}
	
	public DataSourceConnect(DruidDataSource dataSource) {
		this.dataSource = dataSource;
		dataSource.setConnectionErrorRetryAttempts(RETRY_CONNECT);
		dataSource.setBreakAfterAcquireFailure(true);
	}
	
	public NamedParameterJdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			synchronized (DataSourceConnect.class) {
				if (jdbcTemplate == null) {
					if (dataSource == null) { 
						dataSource = new DruidDataSource();
						dataSource.setUrl(url);
						dataSource.setUsername(userName);
						dataSource.setPassword(password);
					}
					this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
					log.info("NamedParameterJdbcTemplate connection success url: {}", dataSource.getUrl());
				}
			}
		}
		return jdbcTemplate;
	}

	public JdbcTemplate getOrgJdbcTemplate() {
		if (orgJdbcTemplate == null) {
			synchronized (DataSourceConnect.class) {
				if (orgJdbcTemplate == null) {
					if (dataSource == null) {
						dataSource = new DruidDataSource();
						dataSource.setUrl(url);
						dataSource.setUsername(userName);
						dataSource.setPassword(password);
					}
					this.orgJdbcTemplate = new JdbcTemplate(dataSource);
					log.info("JdbcTemplate connection success url: {}", dataSource.getUrl());
				}
			}
		}
		return orgJdbcTemplate;
	}
	
	public void close(DruidDataSource dataSource) {
		if (dataSource.isClosed() == false) {
			dataSource.close();
		}
	}
}
