package com.clubfactory.platform.scheduler.web.client.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TableLineageGraphVo implements Serializable {

    private Long id;
    private String dbName;
    private String dbHost;
    private String tableName;
    private String owner;
    private String departName;
    private String description;
    private String dataLastTime;

    private List<TableLineageGraphVo> parents;
    private List<TableLineageGraphVo> childs;
}
