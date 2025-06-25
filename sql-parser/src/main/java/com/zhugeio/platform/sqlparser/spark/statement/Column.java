package com.zhugeio.platform.sqlparser.spark.statement;

public class Column extends SparkStmt {

    private String name;
    private String alia;
    private String type;
    private String comment;


    public Column(String name, String alia, String type, String comment) {
        this.name = name;
        this.alia = alia;
        this.type = type;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlia() {
        return alia;
    }

    public void setAlia(String alia) {
        this.alia = alia;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
