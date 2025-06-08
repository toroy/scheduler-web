/**
 * File generated at: 2018年11月21日下午4:38:34
 */
package com.zhugeio.platform.scheduler.web.core.jdbc.client;

import java.util.List;
import java.util.Map;

import com.zhugeio.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.zhugeio.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.zhugeio.platform.scheduler.web.core.jdbc.dto.TableDto;

/**
 * 这里写功能介绍 
 * 
 * @author 周利江
 * @Date 2018年11月21日下午4:38:34
 *
 */
public interface Operations {
	
	/**
	 * 初始化
	 * 
	 * @param dbDto
	 * @author 周利江
	 * @Date 2018年11月23日上午10:58:58
	 */
	public void init(DbDto dbDto, String dbUrl);
	
	/**
	 * 获取所有表名
	 * 
	 * @param dbUrl 链接url
	 * @return
	 * @author 周利江
	 * @Date 2018年11月21日下午5:48:21
	 */
	public List<TableDto> listTables(String dbUrl);
	
	/**
	 * 获取表结构
	 * 
	 * @param dbUrl 链接url
	 * @param tableName 表名
	 * @return
	 * @author 周利江
	 * @Date 2018年11月21日下午5:24:10
	 */
	public List<ColumnDto> listColumns(String dbUrl, String tableName);


	/**
	 * 获取表结构
	 *
	 * @param dbUrl 链接url
	 * @param dbName 库名
	 * @param tableName 表名
	 * @return
	 * @author 周利江
	 * @Date 2018年11月21日下午5:24:10
	 */
	public List<ColumnDto> listColumns(String dbUrl, String dbName, String tableName);
	
	/**
	 * 获取数据
	 * 
	 * @param dbUrl 链接url
	 * @param tableName 表名
	 * @return
	 * @author 周利江
	 * @Date 2018年11月21日下午5:24:50
	 */
	public List<Map<String, Object>> queryForList(String dbUrl, String tableName);
	
	/**
	 * 执行sql
	 * 
	 * @param sql
	 * @param dbUrl dbUrl 链接url
	 */
	public void execute(String sql, String dbUrl);
	
	/**
	 * 关闭资源
	 * 
	 * @param dbId
	 * @author 周利江
	 * @Date 2018年11月27日下午3:26:47
	 */
	public void close(String dbUrl);
	
}
