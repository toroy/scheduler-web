package com.zhugeio.platform.sqlparser.lineage.entity;

import lombok.Data;

import java.util.List;

@Data
public class TableLineageSp {

    private List<String> inputTables;

    private String outputTable;


    public TableLineageSp(List<String> inputTables, String outputTable) {
        this.inputTables = inputTables;
        this.outputTable = outputTable;
    }
}
