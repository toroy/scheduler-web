package com.zhugeio.platform.meta.client.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TblUpdateDto implements Serializable {

    private String oldDbHost;
    private String newDbHost;

    public TblUpdateDto(String oldDbHost, String newDbHost) {
        this.oldDbHost = oldDbHost;
        this.newDbHost = newDbHost;
    }
}

