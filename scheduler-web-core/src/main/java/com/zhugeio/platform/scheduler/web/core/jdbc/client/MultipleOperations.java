/**
 * File generated at: 2018年10月18日上午11:27:42
 */
package com.zhugeio.platform.scheduler.web.core.jdbc.client;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.zhugeio.platform.scheduler.web.core.jdbc.connect.ConnectService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * 多个数据源操作类
 *
 * @author 周利江
 * @Date 2018年10月18日上午11:27:42
 */
@Service
public class MultipleOperations {

	@Resource
    ConnectService connectService;

    public MultipleOperations(ConnectService connectService) {
        this.connectService = connectService;
    }

    /**
     * 查询数据
     *
     * @param sql
     * @param dbId    数据源id
     * @param dataMap 参数
     * @return
     */
    public List<Map<String, Object>> queryForList(@NonNull String sql, Map<String, Object> dataMap,@NonNull String dbUrl) {
        NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbUrl);
        return jdbcTemplate.queryForList(sql, dataMap);
    }

    /**
     * 查询总数
     *
     * @param sql
     * @param dataMap
     * @param dbId
     * @return
     */
    public Long total(@NonNull String sql, Map<String, Object> dataMap,@NonNull String dbUrl) {
        NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbUrl);
        return jdbcTemplate.queryForObject(sql, dataMap, Long.class);
    }

    /**
     * 执行sql
     *
     * @param sql
     * @param dbId
     */
    public void execute(@NonNull String sql,@NonNull String dbUrl) {
        NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbUrl);
        jdbcTemplate.execute(sql, new PreparedStatementCallback<Boolean>() {
            public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.execute();
                return null;
            }
        });
    }

    /**
     * 执行sql
     *
     * @param sql
     * @param dbId
     */
    public void executeBatch(String[] sql, String dbUrl) {
        JdbcTemplate jdbcTemplate = connectService.getOrgJdbcTemplate(dbUrl);
        jdbcTemplate.batchUpdate(sql);
    }
    
    
    public int[] executeBatch(String sql, List<Map<String, Object>> datas, String dbUrl) {
    	Map<String, ?>[] batchValues = new Map[datas.size()];
    	for (int i = 0; i < datas.size(); i++) {
    		batchValues[i] = datas.get(i);
    	}
    	NamedParameterJdbcTemplate jdbcTemplate = connectService.getJdbcTemplate(dbUrl);
    	return jdbcTemplate.batchUpdate(sql, batchValues);
    }

    public void executeBatchArgs(String sql, List<Object[]> args,  String dbUrl) {
        JdbcTemplate jdbcTemplate = connectService.getOrgJdbcTemplate(dbUrl);
        jdbcTemplate.batchUpdate(sql, args);

    }

}
