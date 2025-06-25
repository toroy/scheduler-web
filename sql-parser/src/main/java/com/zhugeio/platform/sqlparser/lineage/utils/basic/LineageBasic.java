package com.zhugeio.platform.sqlparser.lineage.utils.basic;

import com.zhugeio.platform.sqlparser.lineage.entity.TableLineageSp;
import com.zhugeio.platform.sqlparser.constants.*;
import com.zhugeio.platform.sqlparser.spark.parser.SparkSqlParserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineageBasic {

    public static final String _COLUMN_REPLACE_REGEX_ = "_column_replace_regex_";
    public static final String _VAR_PLACEHOLDER_ = "_var_placeholder_";
    public static final String _VAR_PLACEHOLDER_S_ = _VAR_PLACEHOLDER_ + "%s_";
    /**
     * 变量替换正则
     */
    public static final String REGEX_REPLACE_PARAMS = "\\$\\{.*?\\}|\\$[a-zA-Z_]+[a-z-A-Z_0-9]+";
    public static final Pattern regex_params_pattern = Pattern.compile(LineageBasic.REGEX_REPLACE_PARAMS);
    /**
     * 排除列替换正则
     */
    private static final String regex_replace_exclude = "`\\(.*?\\).+?`";

    public static List<String> getSqlsFromRawStr(String rawSqlsStr) {
        List<String> sqls = new ArrayList<>();
        if (StringUtils.isBlank(rawSqlsStr)) {
            return sqls;
        }
        //替换参数
        Matcher m = regex_params_pattern.matcher(rawSqlsStr);
        Set<String> set = new HashSet<>();
        while (m.find()) {
            set.add(m.group());
        }
        int idx = 0;
        for (String toReplace : set) {
            rawSqlsStr = rawSqlsStr.replace(toReplace, String.format(_VAR_PLACEHOLDER_S_, idx));
            idx++;
        }

        //替换排除列：select `(name|id|pwd)?+.+` from table
        rawSqlsStr = rawSqlsStr.replaceAll(regex_replace_exclude, _COLUMN_REPLACE_REGEX_);
        //替换 “ ` ”
        rawSqlsStr = rawSqlsStr.trim().replace("`", "");
        String[] arr = rawSqlsStr.split("(?<!\\\\);");

        for (String sql : arr) {
            String sqlTrim = sql.toLowerCase().trim();
            if (sqlTrim.startsWith("set") || sqlTrim.startsWith("add") || StringUtils.isBlank(sqlTrim)) {
                continue;
            }

            //sql 内部的注释，会被自动识别
            sqls.add(sql);
        }

        int sqlIdx = 0;
        int newBeginSqlIdx = -1;
        List<String> realSqls = new ArrayList<>();
        for (String sql : sqls) {
            if (sqlIdx <= newBeginSqlIdx) {
                sqlIdx++;
                continue;
            }
            MutableInt mutSqlIdx = new MutableInt(sqlIdx);
            recursionCheckSql(realSqls, sqls, sql, mutSqlIdx);
            newBeginSqlIdx = mutSqlIdx.getValue();
            sqlIdx++;
        }
        return realSqls;
    }

    private static void recursionCheckSql(List<String> realSqls, List<String> sqls, String sql, MutableInt mutSqlIdx) {
        try {
            SparkSqlParserUtils.checkSqlValid(sql);
            realSqls.add(sql);
        } catch (Exception e) {
            mutSqlIdx.increment();
            if (mutSqlIdx.getValue() < sqls.size()) {
                sql += ";" + sqls.get(mutSqlIdx.getValue());
                recursionCheckSql(realSqls, sqls, sql, mutSqlIdx);
            }
        }

    }


    protected static Set<String> getInputTableList(String curUseDb, Set<String> inputTables) {
        Set<String> inputTableSet = new HashSet<>();
        for (String ele : inputTables) {
            ele = checkDb(curUseDb, ele);
            if (StringUtils.isNotBlank(ele)) {
                inputTableSet.add(ele);
            }
        }
        return inputTableSet;
    }


    protected static String checkDb(String curUseDb, String table) {
        if (!table.contains(Punc.DOT) && StringUtils.isNotBlank(curUseDb)) {
            table = curUseDb + Punc.DOT + table;
        }
        return table;
    }


    protected static List<TableLineageSp> getTableLineageSpsFrom(
            Map<String, List<Set<String>>> outInputsMap) {
        List<TableLineageSp> tableLineageSps = new ArrayList<>();
        Set<Map.Entry<String, List<Set<String>>>> entrys = outInputsMap.entrySet();
        for (Map.Entry<String, List<Set<String>>> entry : entrys) {
            String out = entry.getKey();
            if (out.contains(_VAR_PLACEHOLDER_)) {
                continue;
            }
            List<Set<String>> inputSets = entry.getValue();
            Set<String> newInputs = new HashSet<>();

            recursionGetRealInputs(outInputsMap, inputSets, newInputs);
            newInputs.remove(out);
            if (CollectionUtils.isNotEmpty(newInputs)) {
                tableLineageSps.add(new TableLineageSp(new ArrayList<String>(newInputs), out));
            }
        }
        return tableLineageSps;
    }


    private static void recursionGetRealInputs(
            Map<String, List<Set<String>>> outInputsMap, List<Set<String>> inputSets, Set<String> newInputs) {
        for (Set<String> inputSet : inputSets) {
            for (String input : inputSet) {
                if (input.contains(_VAR_PLACEHOLDER_)) {
                    List<Set<String>> moreInputs = outInputsMap.get(input);
                    if (CollectionUtils.isNotEmpty(moreInputs)) {
                        recursionGetRealInputs(outInputsMap, moreInputs, newInputs);
                    }
                } else {
                    if (!input.contains(_VAR_PLACEHOLDER_)) {
                        newInputs.add(input);
                    }
                }
            }
        }
    }

}
