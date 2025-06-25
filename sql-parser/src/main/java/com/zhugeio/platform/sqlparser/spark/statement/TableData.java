package com.zhugeio.platform.sqlparser.spark.statement;


import com.zhugeio.platform.sqlparser.spark.enums.InsertMode;

import java.util.ArrayList;
import java.util.List;

public class TableData extends SparkStmt {

    private List<TableSource> inputTables = new ArrayList<>();
    private List<TableSource> outpuTables = new ArrayList<>();
    private Integer limit;
    private InsertMode insertMode;

    public List<TableSource> getInputTables() {
        return inputTables;
    }

    public void setInputTables(List<TableSource> inputTables) {
        this.inputTables = inputTables;
    }

    public List<TableSource> getOutpuTables() {
        return outpuTables;
    }

    public void setOutpuTables(List<TableSource> outpuTables) {
        this.outpuTables = outpuTables;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public InsertMode getInsertMode() {
        return insertMode;
    }

    public void setInsertMode(InsertMode insertMode) {
        this.insertMode = insertMode;
    }
}
