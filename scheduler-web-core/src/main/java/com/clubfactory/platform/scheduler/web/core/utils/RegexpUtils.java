package com.clubfactory.platform.scheduler.web.core.utils;

import com.clubfactory.platform.scheduler.common.util.Assert;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiejiajun
 */
public class RegexpUtils {

    private static final Pattern dbNameExtractor = Pattern.compile("^jdbc:.*//.*/(.*?)([;?]|$)");
    private static final Pattern dbServerHostExtractor = Pattern.compile("^jdbc:.*//(.*?)([:/]).*?([;?]|$)");
    private static final Pattern dbServerPortExtractor = Pattern.compile("^jdbc:.*//.*?:(\\d{4,})[/;?].*");

    /**
     * 从JDBC URL中提取DbName
     * @param jdbcUrl
     * @return
     */
    public static String extractDbName(String jdbcUrl){
        Assert.notNull(jdbcUrl);
        Matcher matcher = dbNameExtractor.matcher(jdbcUrl);
        String dbName = null;
        if (matcher.find()){
            dbName = matcher.group(1);
        }
       return StringUtils.isEmpty(dbName) ? null : dbName;
    }

    /**
     * 从JDBC URL中提取DB Server Host
     * @param jdbcUrl
     * @return
     */
    public static String extractDbServerHost(String jdbcUrl){
        Assert.notNull(jdbcUrl);
        Matcher matcher = dbServerHostExtractor.matcher(jdbcUrl);
        String dbName = null;
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        return StringUtils.isEmpty(dbName) ? null : dbName;
    }


    /**
     * 从JDBC URL中提取DB Server Port
     * @param jdbcUrl
     * @return
     */
    public static String extractDbServerPort(String jdbcUrl){
        Assert.notNull(jdbcUrl);
        Matcher matcher = dbServerPortExtractor.matcher(jdbcUrl);
        String dbName = null;
        if (matcher.find()){
            dbName = matcher.group(1);
        }
        return StringUtils.isEmpty(dbName) ? null : dbName;
    }

    public static void main(String[] args) {
        System.out.println(extractDbServerPort("jdbc:hive2://localhost:10002/"));
        System.out.println(extractDbServerPort("jdbc:hive2://localhost:10002/default;"));
        System.out.println(extractDbServerPort("jdbc:hive2://localhost:10002;"));
        System.out.println(extractDbServerPort("jdbc:hive2://localhost:10002?"));
        System.out.println(extractDbServerPort("jdbc:mysql://localhost/default"));
        System.out.println(extractDbServerPort("jdbc:mysql://localhost:3306/default?xxxawd"));
    }
}
