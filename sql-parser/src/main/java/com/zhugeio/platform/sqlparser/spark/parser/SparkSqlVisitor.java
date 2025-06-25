package com.zhugeio.platform.sqlparser.spark.parser;

import com.zhugeio.platform.sqlparser.spark.autogen.SparkSqlBaseBaseVisitor;
import com.zhugeio.platform.sqlparser.spark.autogen.SparkSqlBaseParser;
import com.zhugeio.platform.sqlparser.spark.enums.InsertMode;
import com.zhugeio.platform.sqlparser.spark.enums.SparkStmtType;
import com.zhugeio.platform.sqlparser.spark.statement.SparkStmtData;
import com.zhugeio.platform.sqlparser.spark.statement.TableData;
import com.zhugeio.platform.sqlparser.spark.statement.TableSource;
import com.zhugeio.platform.sqlparser.constants.*;
import com.zhugeio.platform.sqlparser.utils.ParserUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SparkSqlVisitor extends SparkSqlBaseBaseVisitor<SparkStmtData> {

    private static final Logger LOG = LoggerFactory.getLogger(SparkSqlVisitor.class);

    private SparkStmtType currentOptType = SparkStmtType.UNKNOWN;
    private TableData tableData = new TableData();
    private Set<String> withNames = new HashSet<>();
    private Integer limit = null;
    private InsertMode insertMode = null;
    private String querySql = null;

    public void setQuerySql(String querySql) {
        this.querySql = querySql;
    }

    public Set<String> getWithNames() {
        return withNames;
    }

    @Override
    public SparkStmtData visitSingleStatement(SparkSqlBaseParser.SingleStatementContext ctx) {
        super.visitSingleStatement(ctx);
        return new SparkStmtData(currentOptType, tableData);
    }

    @Override
    public SparkStmtData visitCreateTable(SparkSqlBaseParser.CreateTableContext ctx) {
        if (ctx.query() == null) {
            currentOptType = SparkStmtType.CREATE_TABLE;
        } else {
            currentOptType = SparkStmtType.CREATE_TABLE_AS_SELECT;
        }
        SparkSqlBaseParser.CreateTableHeaderContext cthCtx = ctx.createTableHeader();
        if (cthCtx == null) {
            LOG.error("visitCreateTable find CreateTableHeaderContext is null, which sql: " + querySql);
            return super.visitCreateTable(ctx);
        }
        TableSource tableSource = ParserUtils.getTableSourceFromTableIde(cthCtx.tableIdentifier());
        tableData.getOutpuTables().add(tableSource);
        return super.visitCreateTable(ctx);
    }

    @Override
    public SparkStmtData visitCreateHiveTable(SparkSqlBaseParser.CreateHiveTableContext ctx) {
        if (ctx.query() == null) {
            currentOptType = SparkStmtType.CREATE_TABLE;
        } else {
            currentOptType = SparkStmtType.CREATE_TABLE_AS_SELECT;
        }
        SparkSqlBaseParser.CreateTableHeaderContext cthCtx = ctx.createTableHeader();
        if (cthCtx == null) {
            LOG.error("visitCreateHiveTable find CreateTableHeaderContext is null, which sql: " + querySql);
            return super.visitCreateHiveTable(ctx);
        }
        TableSource tableSource = ParserUtils.getTableSourceFromTableIde(cthCtx.tableIdentifier());
        tableData.getOutpuTables().add(tableSource);
        return super.visitCreateHiveTable(ctx);
    }


    @Override
    public SparkStmtData visitStatementDefault(SparkSqlBaseParser.StatementDefaultContext ctx) {
        if(StringUtils.equalsIgnoreCase(Key.SELECT, ctx.start.getText())) {
            currentOptType = SparkStmtType.SELECT;
            return super.visitQuery(ctx.query());

        } else if(StringUtils.equalsIgnoreCase(Key.INSERT, ctx.start.getText())) {
            return super.visitQuery(ctx.query());

        } else if(StringUtils.equalsIgnoreCase(Key.FROM, ctx.start.getText())) {
            currentOptType = SparkStmtType.MULTI_INSERT;
            return super.visitQuery(ctx.query());
        }
        return super.visitStatementDefault(ctx);
    }

    @Override
    public SparkStmtData visitTableIdentifier(SparkSqlBaseParser.TableIdentifierContext ctx) {
        TableSource tableSource = ParserUtils.getTableSourceFromTableIde(ctx);
        tableData.getInputTables().add(tableSource);
        return super.visitTableIdentifier(ctx);
    }


    @Override
    public SparkStmtData visitInlineTableDefault1(SparkSqlBaseParser.InlineTableDefault1Context ctx) {
        currentOptType = SparkStmtType.CREATE_TABLE_AS_SELECT;
        return super.visitInlineTableDefault1(ctx);
    }


    @Override
    public SparkStmtData visitQuerySpecification(SparkSqlBaseParser.QuerySpecificationContext ctx) {
        currentOptType = SparkStmtType.CREATE_TABLE_AS_SELECT;
        return super.visitQuerySpecification(ctx);
    }


    @Override
    public SparkStmtData visitFromClause(SparkSqlBaseParser.FromClauseContext ctx) {
        return super.visitFromClause(ctx);
    }


//    @Override
//    public SparkStmtData visitMultiInsertQueryBody(SparkSqlBaseParser.MultiInsertQueryBodyContext ctx) {
//        SparkSqlBaseParser.InsertIntoContext obj = ctx.insertInto();
//        if (obj instanceof SparkSqlBaseParser.InsertOverwriteTableContext) {
//            SparkSqlBaseParser.TableIdentifierContext tableIdentifier =
//                    ((SparkSqlBaseParser.InsertOverwriteTableContext) obj).tableIdentifier();
//
//            TableSource tableSource = ParserUtils.getTableSourceFromTableIde(tableIdentifier);
//            tableData.getOutpuTables().add(tableSource);
//
//        } else if (obj instanceof SparkSqlBaseParser.InsertIntoTableContext) {
//            SparkSqlBaseParser.TableIdentifierContext tableIdentifier =
//                    ((SparkSqlBaseParser.InsertIntoTableContext) obj).tableIdentifier();
//
//            TableSource tableSource = ParserUtils.getTableSourceFromTableIde(tableIdentifier);
//            tableData.getOutpuTables().add(tableSource);
//        }
//        return super.visitMultiInsertQueryBody(ctx);
//    }

    @Override
    public SparkStmtData visitInsertIntoTable(SparkSqlBaseParser.InsertIntoTableContext ctx) {
        insertMode = InsertMode.INTO;
        TableSource tableSource = ParserUtils.getTableSourceFromTableIde(ctx.tableIdentifier());
        tableData.getOutpuTables().add(tableSource);
        return super.visitInsertIntoTable(ctx);
    }

    @Override
    public SparkStmtData visitInsertOverwriteTable(SparkSqlBaseParser.InsertOverwriteTableContext ctx) {
        insertMode = InsertMode.OVERWRITE;
        TableSource tableSource = ParserUtils.getTableSourceFromTableIde(ctx.tableIdentifier());
        tableData.getOutpuTables().add(tableSource);
        return super.visitInsertOverwriteTable(ctx);
    }

    @Override
    public SparkStmtData visitCtes(SparkSqlBaseParser.CtesContext ctx) {
        List<SparkSqlBaseParser.NamedQueryContext> nqs = ctx.namedQuery();
        for (SparkSqlBaseParser.NamedQueryContext nq : nqs) {
            SparkSqlBaseParser.IdentifierContext name = nq.name;
            if (name != null) {
                withNames.add(name.getText());
            }
        }
        return super.visitCtes(ctx);
    }


    //    @Override
//    public SparkStmtData visit(SparkSqlBaseParser.Context ctx) {
//
//        return super.visit(ctx);
//    }

}
