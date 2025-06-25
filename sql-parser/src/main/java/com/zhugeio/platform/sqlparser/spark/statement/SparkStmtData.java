package com.zhugeio.platform.sqlparser.spark.statement;

import com.zhugeio.platform.sqlparser.spark.enums.SparkStmtType;
import com.zhugeio.platform.sqlparser.spark.statement.SparkStmt;

public class SparkStmtData {

    private SparkStmtType sparkStmtType;
    private SparkStmt SparkStmt;

    public SparkStmtData(SparkStmtType sparkStmtType, SparkStmt sparkStmt) {
        this.sparkStmtType = sparkStmtType;
        SparkStmt = sparkStmt;
    }

    public SparkStmtData(SparkStmtType sparkStmtType) {
        this.sparkStmtType = sparkStmtType;
    }

    public SparkStmtData() {
    }


    public SparkStmtType getSparkStmtType() {
        return sparkStmtType;
    }

    public void setSparkStmtType(SparkStmtType sparkStmtType) {
        this.sparkStmtType = sparkStmtType;
    }

    public SparkStmt getSparkStmt() {
        return SparkStmt;
    }

    public void setSparkStmt(SparkStmt sparkStmt) {
        SparkStmt = sparkStmt;
    }
}
