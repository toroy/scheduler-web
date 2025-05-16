/**
 * File generated at: 2018年11月23日上午10:28:23
 */
package com.bigdata.platform.scheduler.web.core.jdbc.dto;

import java.io.Serializable;

import com.bigdata.platform.scheduler.dal.enums.DbType;


/**
 * db请求对象
 * 
 * @author 周利江
 * @Date 2018年11月23日上午10:28:23
 *
 */
public class DbDto implements Serializable {

	private static final long serialVersionUID = 779854011529174503L;

	/**
	 * 账号
	 */
	private String dbUser;
	
	/**
	 * 类型
	 */
	private DbType dbType;
	
	/**
	 * 密码
	 */
	private String dbPwd;
	
	/**
	 * 库名
	 */
	private String dbName;
	
	/**
	 * 地址
	 */
	private String dbHost;
	
	/**
	 * 端口地址
	 */
	private Integer dbPort;
	
	/**
	 * jdbcUrl链接，使用这个dbPort, dbHost作废
	 */
	private String dbUrl;

	/**
	 * 连接属性
	 */
	private String prop;
	
	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public DbType getDbType() {
		return dbType;
	}

	public void setDbType(DbType dbType) {
		this.dbType = dbType;
	}

	public String getDbPwd() {
		return dbPwd;
	}

	public void setDbPwd(String dbPwd) {
		this.dbPwd = dbPwd;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbHost() {
		return dbHost;
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public Integer getDbPort() {
		return dbPort;
	}

	public void setDbPort(Integer dbPort) {
		this.dbPort = dbPort;
	}

	public String getProp() {
		return prop;
	}

	public void setProp(String prop) {
		this.prop = prop;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof DbDto) {
			DbDto dbDto = (DbDto) obj;
			return dbHost.equals(dbDto.getDbHost())
					&& dbType.equals(dbDto.getDbType())
					&& dbPort.equals(dbDto.getDbPort())
					&& dbName.equals(dbDto.getDbName());
		}
		
		return super.equals(obj);
	}
	
	//@Override
	public int hasCode() {
		return dbHost.hashCode() + dbType.hashCode() + dbPort.hashCode() + dbName.hashCode();
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}
}
