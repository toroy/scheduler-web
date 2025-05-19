package com.bigdata.platform.meta.client.dto;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.bigdata.platform.meta.client.enums.DbType;

public class TableDto extends TableSimpleDto {
    private static final long serialVersionUID = 4686091588659576857L;
    private String dbHost;
    private DbType dbType;

    public TableDto() {
    }

    public String getDbHost() {
        return this.dbHost;
    }

    public DbType getDbType() {
        return this.dbType;
    }

    public void setDbHost(final String dbHost) {
        this.dbHost = dbHost;
    }

    public void setDbType(final DbType dbType) {
        this.dbType = dbType;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TableDto)) {
            return false;
        } else {
            TableDto other = (TableDto)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$dbHost = this.getDbHost();
                Object other$dbHost = other.getDbHost();
                if (this$dbHost == null) {
                    if (other$dbHost != null) {
                        return false;
                    }
                } else if (!this$dbHost.equals(other$dbHost)) {
                    return false;
                }

                Object this$dbType = this.getDbType();
                Object other$dbType = other.getDbType();
                if (this$dbType == null) {
                    if (other$dbType != null) {
                        return false;
                    }
                } else if (!this$dbType.equals(other$dbType)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TableDto;
    }


    public String toString() {
        return "TableDto(dbHost=" + this.getDbHost() + ", dbType=" + this.getDbType() + ")";
    }
}
