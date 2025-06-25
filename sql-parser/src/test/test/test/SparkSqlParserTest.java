package test;

import com.zhugeio.platform.sqlparser.spark.kotlin.StatementData;
import com.zhugeio.platform.sqlparser.spark.kotlin.KotlinSparkSQLHelper;
import com.zhugeio.platform.sqlparser.spark.parser.SparkSqlParserUtils;
import com.zhugeio.platform.sqlparser.spark.statement.SparkStmtData;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SparkSqlParserTest {

    @Test
    public void checkSqlValid() throws Exception {
        SparkSqlParserUtils.checkSqlValid(
                "select * from vas where s = a");
        System.out.println();
    }


    @Test
    public void getStatementData7() throws Exception {
        String filePath = "/Users/chenqian/work_doc/元数据/demo_sqls/table_var_hive_7.sql";
        String sql = IOUtils.toString(new FileInputStream(filePath));
        SparkSqlParserUtils.checkSqlValid(sql);
//        SparkStmtData stmtData = SparkSqlParserUtils.getStatementData(sql);
//        System.out.println(stmtData);
    }


    @Test
    public void kotlinGetStatementData() throws Exception {
        String filePath = "/Users/chenqian/work_doc/元数据/demo_sqls/table_var_hive_7.sql";
        String sql = IOUtils.toString(new FileInputStream(filePath));
        StatementData sd = KotlinSparkSQLHelper.getStatementData(sql);
        System.out.println();
    }

}
