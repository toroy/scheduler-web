package com.zhugeio.platform.sqlparser.lineage.utils;


import com.zhugeio.platform.sqlparser.lineage.entity.TableLineageSp;
import com.zhugeio.platform.sqlparser.lineage.utils.basic.LineageBasic;
import com.zhugeio.platform.sqlparser.constants.*;
import com.zhugeio.platform.sqlparser.spark.parser.SparkSqlParserUtils;
import com.zhugeio.platform.sqlparser.spark.statement.SparkStmt;
import com.zhugeio.platform.sqlparser.spark.statement.SparkStmtData;
import com.zhugeio.platform.sqlparser.spark.statement.TableData;
import com.zhugeio.platform.sqlparser.spark.statement.TableSource;
import com.zhugeio.platform.sqlparser.utils.ParserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class LineageUtils extends LineageBasic {

    private static final Logger LOG = LoggerFactory.getLogger(LineageUtils.class);

    public static List<TableLineageSp> getTableLineagesFromSqls(String rawSqlsStr) {
        List<String> sqls = getSqlsFromRawStr(rawSqlsStr);
        Map<String, List<Set<String>>> outInputsMap = new HashMap<>();
        String curUseDb = "";

        for (String sql : sqls) {
            try {
//                if (sql.startsWith("insert overwrite table dw_ods.branch_table_shipping_ticket_df partition")) {
//                    System.out.println();
//                }
                if (sql.startsWith(Key.USE)) {
                    curUseDb = sql.substring(Key.USE.length());
                    curUseDb = curUseDb.replace(" ", "");
                } else {
                    try {
                        SparkStmtData stmtData = SparkSqlParserUtils.getStatementData(sql);
                        SparkStmt sparkStmt = stmtData.getSparkStmt();
                        if (sparkStmt instanceof TableData) {
                            TableData tableData = (TableData) sparkStmt;
                            List<String> outputTables = ParserUtils.getOutputsListFrom(tableData);
                            Set<String> inputTables = ParserUtils.getInputsSetFrom(tableData);

                            if (CollectionUtils.isEmpty(inputTables)) {
                                continue;
                            }
                            if (outputTables.size() > 1) {
                                LOG.error("find outputTables size > 1, they are " + outputTables);
                            }
                            String outputTable = "";
                            if (!outputTables.isEmpty()) {
                                outputTable = outputTables.get(0);
                            }
                            outputTable = checkDb(curUseDb, outputTable);

                            Set<String> inputTableSet = getInputTableList(curUseDb, inputTables);
                            if (StringUtils.isNotBlank(outputTable)) {
                                MapUtils.fillKeyListMap(outInputsMap, outputTable, inputTableSet);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("", e);
                    }
                }
            } catch (Exception e) {
                LOG.error("", e);
            }
        }

        return getTableLineageSpsFrom(outInputsMap);
    }


}
