package com.clubfactory.platform.scheduler.web.server.vo;

import com.clubfactory.platform.scheduler.web.core.enums.SortType;
import lombok.Data;

import java.io.Serializable;

@Data
public class SortByVo implements Serializable {

    private String fieldName;

    private SortType sortType;

}
