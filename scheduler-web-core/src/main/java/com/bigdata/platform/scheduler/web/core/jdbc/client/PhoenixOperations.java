package com.bigdata.platform.scheduler.web.core.jdbc.client;

import com.bigdata.platform.scheduler.web.core.jdbc.dto.ColumnDto;
import com.bigdata.platform.scheduler.web.core.jdbc.dto.DbDto;
import com.bigdata.platform.scheduler.web.core.jdbc.dto.TableDto;
import com.bigdata.platform.scheduler.common.util.Assert;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.List;

/**
 * Phoenix操作类
 * @author 陈骞
 * @Date 2020年5月19日
 *
 * Phoenix 没有用户名、密码，只能通过 kerberos 实现加密登录。
 */
@Slf4j
public class PhoenixOperations extends BaseOperations implements Operations {


    @Override
    public void init(DbDto dbDto, String dbUrl) {
        super.init(dbDto, dbUrl);
    }


    @Override
    public List<TableDto> listTables(String dbUrl) {
        return Lists.newArrayList();
    }


    @Override
    public List<ColumnDto> listColumns(String dbUrl, String tableName) {
        Assert.notNull(dbUrl);
        Assert.notBlank(tableName, "表名");
        List<ColumnDto> columnDtos = Lists.newArrayList();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
//            Class.forName(DbType.PHOENIX.getDriverClassName());
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
            conn = DriverManager.getConnection(dbUrl);
//            String sql = "select * from " + tableName + " order by AGE limit 10";
//            String sql = "desc " + tableName;
            String sql = "select * from " + tableName + " limit 1";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            ResultSetMetaData rsm = rs.getMetaData();
            int colCounts = rsm.getColumnCount();
            for (int i = 1; i <= colCounts; i++) {
                String cn = rsm.getColumnName(i);
                String ct = rsm.getColumnTypeName(i);
                ColumnDto columnDto = new ColumnDto();
                columnDto.setName(cn);
                columnDto.setType(ct);
                columnDto.setDesc("");
                columnDtos.add(columnDto);
            }

        } catch (Exception e) {
            log.error("", e);
        } finally {
            try {
                conn.close();
                stmt.close();
                rs.close();
            } catch (Exception e) {
                //ignore
            }
        }
        return columnDtos;
    }


    @Override
    public List<ColumnDto> listColumns(String dbUrl, String dbName, String tableName) {
        return listColumns(dbUrl, dbName + "." + tableName);
    }


    @Override
    public void close(String dbUrl) {
        Assert.notNull(dbUrl);
        super.close(dbUrl);
    }

}
