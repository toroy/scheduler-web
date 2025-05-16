package com.bigdata.platform.scheduler.web.server.utils;


import com.bigdata.platform.scheduler.dal.enums.DbType;
import com.bigdata.platform.scheduler.web.core.dto.DataSourceDto;
import com.bigdata.platform.scheduler.web.core.utils.DataSourceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataSourceUtilsTest {

    private int timeout;

    @Before
    public void before(){
        this.timeout = 5;
    }

    @Test
    public void testMysql(){
        DataSourceDto dto = new DataSourceDto();
        dto.setDsType(DbType.MYSQL);
        dto.setDsUrl("jdbc:mysql://localhost:3306/test");
        dto.setDsUser("root");
        dto.setDsPassword("root");

        Assert.assertEquals(true,DataSourceUtils.testDataSource(dto,timeout));

    }

    @Test(expected = Exception.class)
    public void testMysqlFailed(){
        DataSourceDto dto = new DataSourceDto();
        dto.setDsType(DbType.MYSQL);
        dto.setDsUser("root");
        dto.setDsPassword("root");

        dto.setDsUrl("jdbc:mysql://localhost1:3306/test");
        DataSourceUtils.testDataSource(dto,timeout);
    }


    @Test
    public void testHive(){
        DataSourceDto dto = new DataSourceDto();
        dto.setDsType(DbType.HIVE);
        dto.setDsUrl("jdbc:hive2://hive-server2-host:10000/default;auth=noSasl");
        dto.setDsUser("hive");

        Assert.assertEquals(true,DataSourceUtils.testDataSource(dto,timeout));
    }


    @Test(expected = Exception.class)
    public void testHiveFailed(){
        DataSourceDto dto = new DataSourceDto();
        dto.setDsType(DbType.HIVE);
        dto.setDsUser("hive");

        dto.setDsUrl("jdbc:hive2://localhost:10000/default");
        DataSourceUtils.testDataSource(dto,timeout);
    }

    @Test
    public void testPG(){
        DataSourceDto dto = new DataSourceDto();
        dto.setDsType(DbType.POSTGRESQL);
        dto.setDsUrl("jdbc:postgresql://34.220.123.60:5432/diff");
        dto.setDsUser("admin");

        Assert.assertEquals(true,DataSourceUtils.testDataSource(dto,timeout));
    }


    @Test(expected = Exception.class)
    public void testPGFailed(){
        DataSourceDto dto = new DataSourceDto();
        dto.setDsType(DbType.POSTGRESQL);
        dto.setDsUser("admin");
        dto.setDsUrl("jdbc:postgresql://localhost:5432/diff");

        DataSourceUtils.testDataSource(dto,timeout);
    }


    @Test
    public void testRedShift(){
        DataSourceDto dto = new DataSourceDto();
        dto.setDsType(DbType.REDSHIFT);
        dto.setDsUrl("jdbc:redshift://aws-host:5439/prodb");
        dto.setDsUser("user");
        dto.setDsPassword("password");

        Assert.assertEquals(true,DataSourceUtils.testDataSource(dto,timeout));
    }

    @Test(expected = Exception.class)
    public void testRedShiftFailed(){
        DataSourceDto dto = new DataSourceDto();
        dto.setDsType(DbType.REDSHIFT);
        dto.setDsUser("user");
        dto.setDsPassword("password");

        dto.setDsUrl("jdbc:redshift://localhost:5439/testdb");

        DataSourceUtils.testDataSource(dto,timeout);
    }
}
