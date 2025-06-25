package com.zhugeio.platform.sqlparser.spark.parser;

import com.zhugeio.platform.sqlparser.basic.ParseErrorListener;
import com.zhugeio.platform.sqlparser.basic.UpperCaseCharStream;
import com.zhugeio.platform.sqlparser.spark.autogen.SparkSqlBaseBaseVisitor;
import com.zhugeio.platform.sqlparser.spark.autogen.SparkSqlBaseLexer;
import com.zhugeio.platform.sqlparser.spark.autogen.SparkSqlBaseParser;
import com.zhugeio.platform.sqlparser.spark.enums.SparkStmtType;
import com.zhugeio.platform.sqlparser.spark.statement.SparkStmtData;
import com.zhugeio.platform.sqlparser.utils.ParserUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkSqlParserUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SparkSqlParserUtils.class);

    /**
     * 如果抛出英文错误栈，是无效sql，使用者自行决定怎么处理错误，可以输出到前端等。
     * @param sql
     * @return
     */
    public static boolean checkSqlValid(String sql) {
        getStatementData(sql, true);
        return true;
    }


    public static SparkStmtData getStatementData(String sql) {
        return getStatementData(sql, false);
    }


    private static SparkStmtData getStatementData(String sql, boolean checkSqlValid) {
        sql = StringUtils.trim(sql);
        UpperCaseCharStream charStream = new UpperCaseCharStream(CharStreams.fromString(sql));
        SparkSqlBaseLexer lexer = new SparkSqlBaseLexer(charStream);
        lexer.removeErrorListeners(); //去除控制台打印错误
        lexer.addErrorListener(new ParseErrorListener());

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SparkSqlBaseParser parser = new SparkSqlBaseParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ParseErrorListener());
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);

        if (checkSqlValid) {
            SparkSqlBaseBaseVisitor visitor = new SparkSqlBaseBaseVisitor();
            visitor.visit(parser.singleStatement());
            return null;

        } else {
            SparkSqlVisitor visitor = new SparkSqlVisitor();
            visitor.setQuerySql(sql);
            SparkStmtData data = null;
            data = visitor.visit(parser.singleStatement());

            if (data == null) {
                data = new SparkStmtData(SparkStmtType.UNKNOWN);
            }
            ParserUtils.checkTables(data, visitor.getWithNames());
            return data;
        }
    }



}
