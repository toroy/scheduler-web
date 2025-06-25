package com.zhugeio.platform.sqlparser.spark.statement;


import com.zhugeio.platform.sqlparser.constants.Punc;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class TableSource extends SparkStmt {

    private String databaseName;
    private String tableName;
    private List<Column> columns;

    public TableSource(String databaseName, String tableName) {
        this.databaseName = databaseName;
        this.tableName = tableName;
    }

    public String getDatabaseName() {
        return databaseName;
    }


    public String getDbAndTbl() {
        String db = StringUtils.isBlank(databaseName) ? "" : databaseName + Punc.DOT;
        return db + tableName;
    }


    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}
