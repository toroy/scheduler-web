package com.bigdata.platform.meta.client.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class TableOwnerDto implements Serializable  {

    private static final long serialVersionUID = -8239792145253475956L;

    /**
     * 用户id
     */
    private String uid;

    /**
     * 名字
     */
    private String name;
}