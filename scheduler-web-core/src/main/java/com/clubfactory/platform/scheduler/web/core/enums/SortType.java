package com.clubfactory.platform.scheduler.web.core.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SortType {

    asc("asc"), desc("desc");

    private String type;

}
