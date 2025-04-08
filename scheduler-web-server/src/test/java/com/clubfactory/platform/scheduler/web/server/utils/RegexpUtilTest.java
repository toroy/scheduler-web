package com.clubfactory.platform.scheduler.web.server.utils;


import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpUtilTest {


    Pattern pattern = Pattern.compile("^jdbc:.*//.*/(.*?)([;?]|$)");



    @Test
    public void dbNameExtractTest(){
        String jdbcUrl = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=gbk&autoReconnect=true&failOverReadOnly=false";
        String dbName = null;
        Matcher matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        Assert.assertEquals("test",dbName);

        jdbcUrl = "jdbc:hive2://localhost:10000/default;";
        matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        Assert.assertEquals("default",dbName);

        jdbcUrl = "jdbc:hive2://localhost:10000/default";
        matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        Assert.assertEquals("default",dbName);

        jdbcUrl = "jdbc:postgresql://localhost:5439/test?useUnicode=true&characterEncoding=utf8";
        matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        Assert.assertEquals("test",dbName);

        jdbcUrl = "jdbc:redshift://examplecluster.abc123xyz789.us-west-2.redshift.amazonaws.com:5439/dev?";
        matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        Assert.assertEquals("dev",dbName);

        jdbcUrl = "jdbc:redshift://examplecluster.abc123xyz789.us-west-2.redshift.amazonaws.com:5439/dev";
        matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        Assert.assertEquals("dev",dbName);


        jdbcUrl = "jdbc:presto://jx-bd-hadoop100.zeus.lianjia.com:8091/hive/default";
        matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        Assert.assertEquals("default",dbName);


        jdbcUrl = "jdbc:presto://jx-bd-hadoop100.zeus.lianjia.com:8091/hive/default?user=hadoop";
        matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        Assert.assertEquals("default",dbName);

        jdbcUrl = "jdbc:hive2://localhost:10000/";
        matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        Assert.assertEquals("",dbName);

        dbName = null;
        jdbcUrl = "jdbc:hive2://localhost:10000";
        matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        Assert.assertEquals(null,dbName);
    }
}
