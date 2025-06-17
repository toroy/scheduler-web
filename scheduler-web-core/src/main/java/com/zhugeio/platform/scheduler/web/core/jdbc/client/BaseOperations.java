/**
 * File generated at: 2018年11月21日下午10:44:57
 */
package com.zhugeio.platform.scheduler.web.core.jdbc.client;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.zhugeio.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.zhugeio.platform.scheduler.web.core.jdbc.connect.ConnectService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.zhugeio.platform.scheduler.common.util.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 这里写功能介绍 
 * 
 * @author 陈泰（周利江）
 * @Date 2018年11月21日下午10:44:57
 *
 */
public class BaseOperations {
	
	@Resource
    ConnectService connectService;
	
	private String SELECT_SQL = "select * from %s limit 20";
	
	public void init(DbDto dbDto, String dbUrl) {
		connectService.init(dbDto, dbUrl);
	}
	
    public void execute(String sql, String dbUrl) {
    	Assert.notNull(dbUrl);
		Assert.notBlank(sql, "sql");
    	
        NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbUrl);
        jdbcTemplate.execute(sql, new PreparedStatementCallback<Boolean>() {
            public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.execute();
                return null;
            }
        });
    }

	public List<Map<String, Object>> queryForList(String dbUrl, String tableName) {
		Assert.notNull(dbUrl);
		Assert.notBlank(tableName, "表名");
		
		NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbUrl);
		if (jdbcTemplate == null) {
			return Lists.newArrayList();
		}
		List<Map<String, Object>> mapDatas = jdbcTemplate.queryForList(String.format(SELECT_SQL, tableName), Maps.newHashMap());
		if (CollectionUtils.isEmpty(mapDatas)) {
			return Lists.newArrayList();
		}
		return mapDatas;
	}
	
	public void close(String dbUrl) {
		connectService.close(dbUrl);
	}
}
