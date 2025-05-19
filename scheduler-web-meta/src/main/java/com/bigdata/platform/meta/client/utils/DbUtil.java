package com.bigdata.platform.meta.client.utils;


import com.bigdata.platform.meta.client.enums.DbType;

public class DbUtil {

    public static final String HIVE_VALUE = "HIVE数仓";

    private static final String UNDER_LINE = "_";

    public static String getDbSource(String dbHost, String dbTypeName) {
        return getDbSource(dbHost, DbType.valueOf(dbTypeName));
    }

    /**
     * 获取统一的数据源信息
     *
     * @param dbHost
     * @param dbType
     * @return
     */
    public static String getDbSource(String dbHost, DbType dbType) {
        if (dbType == null) {
            return dbHost;
        }
        if (DbType.HIVE == dbType) {
            return HIVE_VALUE;
        }
        return dbHost;
    }

    public static String getTableKey(String dbSource, String dbName, String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append(dbSource);
        sb.append(UNDER_LINE);
        sb.append(dbName);
        sb.append(UNDER_LINE);
        sb.append(tableName);
        return sb.toString();
    }
}
